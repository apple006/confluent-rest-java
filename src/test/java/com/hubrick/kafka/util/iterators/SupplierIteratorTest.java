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

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author ahanin
 * @since 1.0.0
 */
public class SupplierIteratorTest {

    @Test
    public void testShouldIterateSuppliedValuesUnlessNullEncountered() {
        final List<Integer> integers = Arrays.asList(1, 2, 3, null, 5);
        final Iterator<Integer> integerIterator = integers.iterator();

        final List<Integer> suppliedValues = Iterators.asStream(new SupplierIterator<>(integerIterator::next))
                .collect(toList());

        assertThat(suppliedValues, equalTo(Arrays.asList(1, 2, 3)));
    }

    @Test
    public void testShouldIteratePollResults() throws Exception {
        final AtomicInteger counter = new AtomicInteger();
        final Iterator<Integer> i = new SupplierIterator<>(counter::incrementAndGet, n -> n <= 100);
        final List<Integer> integers = Iterators.asStream(i)
                .filter(m -> m % 2 == 0)
                .collect(toList());

        final List<Integer> evenIntegers = new ArrayList<>(50);
        for (int z = 2; z <= 100; z += 2) {
            evenIntegers.add(z);
        }

        assertThat(integers, equalTo(evenIntegers));
    }

}
