/**
 * Copyright (C) 2016 Etaia AS (oss@hubrick.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.hubrick.kafka.util.iterators;

import com.hubrick.kafka.util.function.PollingSupplier;
import org.junit.Test;

import java.util.Optional;

import static com.hubrick.kafka.util.iterators.PollingStrategies.escalatingPollingStrategy;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

/**
 * @author ahanin
 * @since 1.0.0
 */
public class PollingSupplierTest {

    @Test
    public void testShouldReturnSuppliedValue() throws Exception {
        Optional<String> result = PollingSupplier.<String>builder().withSupplier(() -> "XYZ").withPollingStrategy((prevDelay) -> 500).build().get();
        assertThat("Must return value supplied by underlying delegate", result.get(), equalTo("XYZ"));
    }

    @Test
    public void testShouldHandleMissingResultGracefully() {
        final Optional<String> result = PollingSupplier.<String>builder()
                .withSupplier(() -> null)
                .withPollingStrategy((prevDelay) -> 500)
                .build()
                .get();
        assertThat("No value must be recorded", result.isPresent(), equalTo(false));
    }

    @Test
    public void testShouldHandleNeverReadResultGracefully() {
        Optional<String> result = PollingSupplier.<String>builder()
                .withSupplier(() -> {
                    try {
                        Thread.sleep(2000L); // sleep for more than the timeout to trigger timeout
                    } catch (InterruptedException e) {
                        // noop
                    }
                    return "XYZ";
                })
                .withPollingStrategy((prevDelay) -> 0)
                .build()
                .get();
        assertThat("No value must be recorded", result.isPresent(), equalTo(false));
    }

    @Test
    public void testShouldBlockEternallyIfTimeoutIsNegative() throws Exception {
        final Thread thread = new Thread(() -> PollingSupplier.<String>builder()
                .withSupplier(() -> null)
                .withPollingStrategy(escalatingPollingStrategy().withTimeout(-1))
                .build()
                .get());
        thread.start();

        try {
            thread.join(10000L);
            assertThat("Thread state", thread.getState(), not(equalTo(Thread.State.TERMINATED)));
        } finally {
            thread.interrupt();
        }
    }

    @Test
    public void testShouldNotBlockIfTimeoutIsZero() throws Exception {
        final Thread thread = new Thread(() -> PollingSupplier.builder()
                .withSupplier(() -> null)
                .withPollingStrategy(escalatingPollingStrategy().withTimeout(0))
                .build()
                .get());
        thread.start();

        try {
            thread.join(1000L);
            assertThat("Thread state", thread.getState(), equalTo(Thread.State.TERMINATED));
        } finally {
            thread.interrupt();
        }
    }

    @Test
    public void testShouldBlockForTimeout() throws Exception {
        final Thread thread = new Thread(() -> PollingSupplier.<String>builder()
                .withSupplier(() -> null)
                .withPollingStrategy(escalatingPollingStrategy().withTimeout(1000L))
                .build()
                .get());
        thread.start();

        thread.join(500L);
        assertThat("Thread state", thread.getState(), not(equalTo(Thread.State.TERMINATED)));

        try {
            thread.join(2000L);
            assertThat("Thread state", thread.getState(), equalTo(Thread.State.TERMINATED));
        } finally {
            thread.interrupt();
        }
    }

    @Test
    public void testShouldNotBlockForNoTimeout() throws Exception {
        final Thread thread = new Thread(() -> PollingSupplier.<String>builder()
                .withSupplier(() -> null)
                .withPollingStrategy(escalatingPollingStrategy().withTimeout(0))
                .build()
                .get());
        thread.start();

        thread.join(500L);
        assertThat("Thread state", thread.getState(), equalTo(Thread.State.TERMINATED));
    }

}
