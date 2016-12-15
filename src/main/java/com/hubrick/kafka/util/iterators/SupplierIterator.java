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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Iterates over supplied values. Iterator emits {@code Supplier} values, until its associated {@link Predicate} returns false.
 * Default predicate tests for non-{@code null} values.
 *
 * @author ahanin
 * @since 1.0.0
 */
public class SupplierIterator<T> implements Iterator<T> {

    private Supplier<T> supplier;

    private LinkedList<T> buffer = new LinkedList<>();

    private Predicate<T> predicate = Objects::nonNull;

    /**
     * @param supplier item {@link Supplier}
     */
    public SupplierIterator(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    /**
     * @param supplier  item {@link Supplier}
     * @param predicate predicate should return {@code false} in order for iterator to emit elements
     */
    public SupplierIterator(Supplier<T> supplier, Predicate<T> predicate) {
        this.supplier = supplier;
        this.predicate = predicate;
    }

    @Override
    public boolean hasNext() {
        return !buffer.isEmpty() || pollToBuffer();
    }

    private boolean pollToBuffer() {
        final T next = supplier.get();
        return predicate.test(next) && buffer.add(next);
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return buffer.pop();
    }

}
