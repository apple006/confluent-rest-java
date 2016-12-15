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

import com.hubrick.kafka.confluent.core.KeyedMessage;
import com.hubrick.kafka.confluent.producer.ConfluentKafkaProducer;
import com.hubrick.kafka.util.DefaultJerseyClientFactory;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author ahanin
 * @since 1.0.0
 */
public class ConfluentKafkaConsumerFunctionalTest {

    private ConfluentKafkaProducer kafkaProducer = new ConfluentKafkaProducer("http://localhost:8082", DefaultJerseyClientFactory.getInstance());

    private ConfluentKafkaConsumer kafkaConsumer;

    @Before
    public void setUp() throws Exception {
        final ConsumerConfig consumerConfig = new ConsumerConfig("tests");
        consumerConfig.setAutoOffsetReset("smallest");
        consumerConfig.setAutoCommitEnable(true);
        consumerConfig.setTimeout(5000L);

        kafkaConsumer = new ConfluentKafkaConsumer("http://localhost:8082", consumerConfig, DefaultJerseyClientFactory.getInstance());
        kafkaConsumer.start();
    }

    @After
    public void tearDown() throws Exception {
        kafkaConsumer.shutdown();
    }

    @Test
    public void testShouldFetchMessages() {
        kafkaProducer.send(KeyedMessage.asKeyedMessage("test", "1"));
        kafkaProducer.send(KeyedMessage.asKeyedMessage("test", "2"));
        kafkaProducer.send(KeyedMessage.asKeyedMessage("test", "3"));

        final Stream<String> records = kafkaConsumer.fetch("test")
                .stream()
                .map(r -> new String(r.getValue()));

        final List<String> messages = records.collect(toList());
        assertThat("Should collect produced messages", messages, equalTo(Arrays.asList("1", "2", "3")));
    }

    @Test
    public void testShouldReturnEmptyMessageCollectionOnFetchingEmptyQueue() {
        final Stream<String> records = kafkaConsumer.fetch("test")
                .stream()
                .map(r -> new String(r.getValue()));

        final List<String> messages = records.collect(toList());
        assertThat("Should return empty collection", messages, Matchers.emptyCollectionOf(String.class));
    }

    @Test
    public void testShouldIterateOverMessages() {
        kafkaProducer.send(KeyedMessage.asKeyedMessage("test", "1"));
        kafkaProducer.send(KeyedMessage.asKeyedMessage("test", "2"));
        kafkaProducer.send(KeyedMessage.asKeyedMessage("test", "3"));

        Iterator<ConsumerRecord> iterator = kafkaConsumer.consume("test");
        final ConsumerRecord a = iterator.next();
        final ConsumerRecord b = iterator.next();
        final ConsumerRecord c = iterator.next();
        final List<String> messages = Arrays.asList(a, b, c).stream()
                .map(r -> new String(r.getValue()))
                .collect(toList());

        assertThat("Should iterate over produced messages", messages, equalTo(Arrays.asList("1", "2", "3")));
    }

    @Test
    public void testShouldThrowNoSuchElementExceptionWhenIteratingOverEmptyQueue() {
        final Iterator<ConsumerRecord> iterator = kafkaConsumer.consume("test");
        try {
            iterator.next();
            fail("Must raise " + NoSuchElementException.class.getName());
        } catch (NoSuchElementException ex) {
            // expected behaviour
        }
    }

    @Test
    public void testShouldAllowCheckingForElementWhenIteratingOverEmptyQueue() throws Exception {
        final Iterator<ConsumerRecord> iterator = kafkaConsumer.consume("test");
        final AtomicBoolean hasCheckedForNext = new AtomicBoolean();
        final Thread thread = new Thread(() -> {
            if (iterator.hasNext()) {
                iterator.next();
            }
            hasCheckedForNext.set(true);
        });
        thread.start();
        thread.join(15000L);

        assertThat("Must have checked if there were elements", hasCheckedForNext.get(), is(true));
    }

    @Test
    public void testShouldStreamMessages() {
        kafkaProducer.send(KeyedMessage.asKeyedMessage("test", "1"));
        kafkaProducer.send(KeyedMessage.asKeyedMessage("test", "2"));
        kafkaProducer.send(KeyedMessage.asKeyedMessage("test", "3"));

        final List<String> messages = kafkaConsumer.stream("test")
                .map(r -> new String(r.getValue()))
                .limit(3)
                .collect(toList());

        assertThat("Should stream produced messages", messages, equalTo(Arrays.asList("1", "2", "3")));
    }

}
