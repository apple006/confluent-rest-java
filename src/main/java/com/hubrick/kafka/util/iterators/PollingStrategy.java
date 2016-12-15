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

/**
 * @author ahanin
 * @since 1.0.0
 */
@FunctionalInterface
public interface PollingStrategy {

    /**
     * Return delay before the next poll.
     *
     * @param lastInterval previous delay, {@code 0} on first call
     * @return delay in milliseconds, must be positive number
     */
    long getInterval(long lastInterval);

    /**
     * Return total poll timeout.
     *
     * @return timeout in milliseconds, {@code 1000} by default
     * <ul>
     * <li>{@code -1} - polling until results available</li>
     * <li>{@code 0} - no polling, results must be available immediately</li>
     * <li>positive number - polling for given number of milliseconds, until results available</li>
     * </ul>
     */
    default long getTimeout() {
        return 1000L;
    }

}
