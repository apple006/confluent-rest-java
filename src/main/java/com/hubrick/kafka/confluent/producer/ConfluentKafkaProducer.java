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
package com.hubrick.kafka.confluent.producer;

import com.hubrick.kafka.confluent.core.ClientFactory;
import com.hubrick.kafka.confluent.core.ClientHelper;
import com.hubrick.kafka.confluent.core.KeyedMessage;

import javax.ws.rs.client.WebTarget;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static javax.ws.rs.client.Entity.entity;

/**
 * @author ahanin
 * @since 1.0.0
 */
public class ConfluentKafkaProducer {

    private final WebTarget kafkaProxyTarget;

    public ConfluentKafkaProducer(final String kafkaProxyEndpoint, ClientFactory instance) {
        kafkaProxyTarget = instance.createClient(ClientHelper.createClientConfig()).target(kafkaProxyEndpoint);
    }

    public void send(final KeyedMessage keyedMessage) {
        final ProducerRecord record = new ProducerRecord();
        record.setKey(asBytes(keyedMessage.getKey()));
        record.setValue(asBytes(keyedMessage.getValue()));
        record.setPartition(keyedMessage.getPartition());

        send(keyedMessage.getTopic(), record);
    }

    public void send(String topic, final ProducerRecord record, final ProducerRecord... additionalRecords) {
        final List<ProducerRecord> producerRecords = Stream.concat(
                singletonList(record).stream(),
                Arrays.stream(additionalRecords)).collect(toList());
        send(topic, producerRecords);
    }

    public void send(String topic, Collection<ProducerRecord> producerRecords) {
        kafkaProxyTarget.path("/topics/" + topic).request()
                .post(entity(new ProducerRequest(producerRecords), "application/vnd.kafka.binary.v1+json"));
    }

    private static <V> byte[] asBytes(V value) {
        if (value == null) {
            return null;
        } else if (value instanceof byte[]) {
            return (byte[]) value;
        } else if (value instanceof CharSequence) {
            final ByteBuffer buffer = Charset.forName("UTF-8").encode(CharBuffer.wrap((CharSequence) value));
            final byte[] bytes = new byte[buffer.remaining()];
            buffer.get(bytes);
            return bytes;
        } else {
            throw new IllegalArgumentException("Unable to encode value: " + value + ". Only byte arrays and char sequences are supported.");
        }
    }

}
