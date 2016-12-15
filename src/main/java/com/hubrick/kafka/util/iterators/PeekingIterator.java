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
import java.util.Optional;

/**
 * @author ahanin
 * @since 1.0.0
 */
public class PeekingIterator<T> implements Iterator<T> {

    private final Iterator<T> delegate;

    private Optional<T> nextValue = Optional.empty();

    public PeekingIterator(Iterator<T> delegate) {
        this.delegate = delegate;
    }

    @Override
    public boolean hasNext() {
        return nextValue != Optional.empty() || delegate.hasNext();
    }

    public T peek() {
        final T value;
        if (nextValue != Optional.empty()) {
            value = nextValue.get();
        } else {
            nextValue = Optional.ofNullable(delegate.next());
            value = nextValue.get();
        }
        return value;
    }

    @Override
    public T next() {
        final T value;
        if (nextValue != Optional.empty()) {
            value = nextValue.get();
            nextValue = Optional.empty();
        } else {
            value = delegate.next();
        }
        return value;
    }

}
