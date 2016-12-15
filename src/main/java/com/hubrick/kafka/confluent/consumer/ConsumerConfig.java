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

/**
 * @author ahanin
 * @since 1.0.0
 */
public class ConsumerConfig {

    private String id;
    private String groupName;
    private String autoOffsetReset;
    private boolean autoCommitEnable;
    private Integer requestMaxBytes;
    private long timeout = -1;

    public ConsumerConfig(String groupName) {
        this.groupName = groupName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getAutoOffsetReset() {
        return autoOffsetReset;
    }

    public void setAutoOffsetReset(String autoOffsetReset) {
        this.autoOffsetReset = autoOffsetReset;
    }

    public boolean getAutoCommitEnable() {
        return autoCommitEnable;
    }

    public void setAutoCommitEnable(boolean autoCommitEnable) {
        this.autoCommitEnable = autoCommitEnable;
    }

    public Integer getRequestMaxBytes() {
        return requestMaxBytes;
    }

    public void setRequestMaxBytes(Integer requestMaxBytes) {
        this.requestMaxBytes = requestMaxBytes;
    }

    public long getTimeout() {
        return timeout;
    }

    public void setTimeout(long timeout) {
        this.timeout = timeout;
    }
}
