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
package com.hubrick.kafka.confluent.consumer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author ahanin
 * @since 1.0.0
 */
public class ConsumerCreationRequest {

    @JsonProperty("id")
    private final String instanceId;

    @JsonProperty("format")
    private final String format;

    @JsonProperty("auto.offset.reset")
    private String autoOffsetReset;

    @JsonProperty("auto.commit.enable")
    private boolean autoCommitEnable;

    @JsonCreator
    protected ConsumerCreationRequest(@JsonProperty("id") String id,
                                      @JsonProperty("format") String format,
                                      @JsonProperty("auto.offset.reset") String autoOffsetReset,
                                      @JsonProperty("auto.commit.enable") boolean autoCommitEnable) {
        this.instanceId = id;
        this.format = format;
        this.autoOffsetReset = autoOffsetReset;
        this.autoCommitEnable = autoCommitEnable;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public String getFormat() {
        return format;
    }

    public String getAutoOffsetReset() {
        return autoOffsetReset;
    }

    public boolean getAutoCommitEnable() {
        return autoCommitEnable;
    }
}
