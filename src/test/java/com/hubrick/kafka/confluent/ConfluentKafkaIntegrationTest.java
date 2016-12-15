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
package com.hubrick.kafka.confluent;

import com.hubrick.kafka.confluent.consumer.ConfluentKafkaConsumer;
import com.hubrick.kafka.confluent.consumer.ConsumerConfig;
import com.hubrick.kafka.confluent.consumer.ConsumerRecord;
import com.hubrick.kafka.confluent.core.KeyedMessage;
import com.hubrick.kafka.confluent.producer.ConfluentKafkaProducer;
import com.hubrick.kafka.util.DefaultJerseyClientFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @author ahanin
 * @since 1.0.0
 */
public class ConfluentKafkaIntegrationTest {

    private ConfluentKafkaProducer kafkaProducer;
    private ConfluentKafkaConsumer kafkaConsumer;

    @Before
    public void setUp() throws Exception {
        kafkaProducer = new ConfluentKafkaProducer("http://localhost:8082",
                DefaultJerseyClientFactory.getInstance());

        final ConsumerConfig consumerConfig = new ConsumerConfig("tests");
        consumerConfig.setAutoOffsetReset("smallest");
        consumerConfig.setAutoCommitEnable(true);
        kafkaConsumer = new ConfluentKafkaConsumer("http://localhost:8082", consumerConfig,
                DefaultJerseyClientFactory.getInstance());
        kafkaConsumer.start();
    }

    @After
    public void tearDown() throws Exception {
        kafkaConsumer.shutdown();
    }

    @Test
    public void testShouldProduceMessage() {
        kafkaProducer.send(KeyedMessage.asKeyedMessage("test", "Here we go"));
        final Iterator<ConsumerRecord> messages = kafkaConsumer.consume("test");
        assertThat("Must read produced message", messages.hasNext(), equalTo(true));
    }

}
