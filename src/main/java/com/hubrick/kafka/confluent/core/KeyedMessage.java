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
package com.hubrick.kafka.confluent.core;

/**
 * @author ahanin
 * @since 1.0.0
 */
public class KeyedMessage<K, V> {

    private final String topic;
    private final K key;
    private final Integer partition;
    private final V value;

    public KeyedMessage(String topic, K key, Integer partition, V value) {
        this.topic = topic;
        this.key = key;
        this.partition = partition;
        this.value = value;
    }

    public static <K, V> KeyedMessage<K, V> asKeyedMessage(String topic, V event) {
        return new KeyedMessage<>(topic, null, null, event);
    }

    public String getTopic() {
        return topic;
    }

    public K getKey() {
        return key;
    }

    public Integer getPartition() {
        return partition;
    }

    public V getValue() {
        return value;
    }
}
