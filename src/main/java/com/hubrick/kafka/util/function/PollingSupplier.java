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
package com.hubrick.kafka.util.function;

import com.hubrick.kafka.util.iterators.PollingStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import static java.util.Optional.empty;

/**
 * Polls provided {@link Supplier} for non-null value while using a delay strategy for waiting between polls.
 *
 * @author ahanin
 * @since 1.0.0
 */
public class PollingSupplier<T> implements Supplier<Optional<T>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(PollingSupplier.class);

    private final Supplier<Optional<T>> delegate;
    private final PollingStrategy pollingStrategy;

    public static <T> PollingSupplierBuilder<T> builder() {
        return new PollingSupplierBuilder<>();
    }

    public static class PollingSupplierBuilder<E> {

        private Supplier<Optional<E>> supplier;
        private PollingStrategy pollingStrategy;

        private PollingSupplierBuilder() {
        }

        public PollingSupplierBuilder<E> withOptionalSupplier(Supplier<Optional<E>> supplier) {
            this.supplier = supplier;
            return this;
        }

        public PollingSupplierBuilder<E> withSupplier(Supplier<E> supplier) {
            this.supplier = () -> Optional.ofNullable(supplier.get());
            return this;
        }

        public PollingSupplierBuilder<E> withPollingStrategy(PollingStrategy pollingStrategy) {
            this.pollingStrategy = pollingStrategy;
            return this;
        }

        public PollingSupplier<E> build() {
            if (supplier == null) {
                throw new IllegalStateException("supplier not set");
            }
            if (pollingStrategy == null) {
                throw new IllegalStateException("pollingStrategy not set");
            }
            return new PollingSupplier<>(this.supplier, pollingStrategy);
        }

    }

    /**
     * Construct {@link PollingSupplier} that will propagate given {@link Supplier}'s
     * {@link Optional} value, unless it is empty {@link Optional}.
     *
     * @param supplier        value providing {@link Supplier}, must return empty {@link Optional} if value missing
     * @param pollingStrategy {@link PollingStrategy} used to determine delay between polls
     */
    private PollingSupplier(Supplier<Optional<T>> supplier, PollingStrategy pollingStrategy) {
        this.delegate = supplier;
        this.pollingStrategy = pollingStrategy;
    }

    /**
     * Poll value.
     *
     * @return {@link Optional} enclosing retrieved value, or empty {@link Optional}, if value was not retrieved
     */
    @Override
    public Optional<T> get() {
        final long timeout = pollingStrategy.getTimeout();
        final AtomicReference<Optional<T>> value = new AtomicReference<>(Optional.<T>empty());

        if (timeout != 0) {
            final Thread pollThread = new Thread(() -> {
                long interval = 0;
                while (!Thread.currentThread().isInterrupted() && (value.get() == empty() || value.get() == null)) {
                    LOGGER.debug("Polling supplier");
                    value.set(delegate.get());

                    if (value.get().isPresent()) {
                        LOGGER.debug("Retrieved a value: {}", value.get().orElse(null));
                    } else if (interval >= 0) {
                        interval = pollingStrategy.getInterval(interval);
                        if (interval > 0) {
                            if (LOGGER.isDebugEnabled()) {
                                long intervalSeconds = interval / 1000;
                                LOGGER.debug("Sleeping for the next {}s",
                                        String.format("%d:%02d:%02d.%03d",
                                                intervalSeconds / 3600,
                                                (intervalSeconds % 3600) / 60,
                                                (intervalSeconds % 60),
                                                interval % 1000));
                            }
                            try {
                                Thread.sleep(interval);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                return;
                            }
                        } else if (interval < 0) {
                            throw new IllegalStateException("Polling strategy suggests negative poll interval: " + interval);
                        }
                    }

                    Thread.yield();
                }
            });

            pollThread.start();

            try {
                pollThread.join(timeout < 0 ? 0 : timeout);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            } finally {
                pollThread.interrupt();
            }
        } else {
            value.set(delegate.get());
        }

        return value.get();
    }

    /**
     * Return simple supplier.
     *
     * @return {@link Supplier} instance that will return {@code value}, if value not found.
     */
    public Supplier<T> toSimpleSupplier() {
        return () -> this.get().orElse(null);
    }

}
