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
public class EscalatingPollingStrategy implements PollingStrategy {

    private long initialInterval = 100;
    private double incrementFactor = 2;
    private long maxInterval = 5000L;
    private long timeout = 10000L;

    public EscalatingPollingStrategy withInitialInterval(long initialInterval) {
        this.initialInterval = initialInterval;
        return this;
    }

    public EscalatingPollingStrategy withIncrementFactor(double incrementFactor) {
        this.incrementFactor = incrementFactor;
        return this;
    }

    public EscalatingPollingStrategy withMaxInterval(long maxInterval) {
        this.maxInterval = maxInterval;
        return this;
    }

    public EscalatingPollingStrategy withTimeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    @Override
    public long getInterval(long lastInterval) {
        final long interval;
        if (lastInterval < 0) {
            interval = 0;
        } else if (lastInterval < 1) {
            interval = initialInterval;
        } else {
            interval = Math.min((int) (lastInterval >= maxInterval ? 1.0 / incrementFactor : incrementFactor) * lastInterval, maxInterval);
        }
        return interval;
    }

    @Override
    public long getTimeout() {
        return timeout;
    }
}
