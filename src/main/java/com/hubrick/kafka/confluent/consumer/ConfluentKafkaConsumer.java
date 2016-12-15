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

import com.hubrick.kafka.confluent.core.ClientFactory;
import com.hubrick.kafka.confluent.core.ClientHelper;
import com.hubrick.kafka.util.concurrent.Locker;
import com.hubrick.kafka.util.function.PollingSupplier;
import com.hubrick.kafka.util.iterators.Iterators;
import com.hubrick.kafka.util.iterators.SupplierIterator;

import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Stream;

import static com.hubrick.kafka.util.iterators.PollingStrategies.escalatingPollingStrategy;
import static java.util.stream.Collectors.toList;

/**
 * @author ahanin
 * @since 1.0.0
 */
public class ConfluentKafkaConsumer {

    private final ConsumerConfig config;

    private final Client client;

    private final WebTarget kafkaProxyTarget;

    private AtomicReference<WebTarget> consumerTarget = new AtomicReference<>();

    private final Locker consumerTargetLocker = new Locker(new ReentrantLock());

    public ConfluentKafkaConsumer(String kafkaProxyEndpoint, ConsumerConfig config, ClientFactory clientFactory) {
        this.config = config;
        this.client = clientFactory.createClient(ClientHelper.createClientConfig());
        this.kafkaProxyTarget = this.client.target(kafkaProxyEndpoint);
    }

    public void start() {
        final ConsumerCreationRequest request = new ConsumerCreationRequest(config.getId(),
                "binary", config.getAutoOffsetReset(), config.getAutoCommitEnable());

        final ConsumerCreationResponse response = kafkaProxyTarget.path("/consumers/" + config.getGroupName())
                .request()
                .post(Entity.entity(request, MediaType.APPLICATION_JSON_TYPE), ConsumerCreationResponse.class);

        setConsumerTarget(client.target(response.getBaseUri()));
    }

    /**
     * Fetch batch of messages.
     *
     * @param topic topic
     * @return batch of available messages, empty if none
     */
    public Collection<ConsumerRecord> fetch(String topic) {
        final Optional<Collection<ConsumerRecord>> records = createFetchPollingSupplier(topic).get();
        return records.orElse(Collections.<ConsumerRecord>emptyList());
    }

    private PollingSupplier<Collection<ConsumerRecord>> createFetchPollingSupplier(String topic) {
        return PollingSupplier.<Collection<ConsumerRecord>>builder()
                .withOptionalSupplier(() -> Optional.of(fetchInternal(topic)).filter(m -> !m.isEmpty()))
                .withPollingStrategy(escalatingPollingStrategy().withTimeout(config.getTimeout()))
                .build();
    }

    private Collection<ConsumerRecord> fetchInternal(String topic) {
        final WebTarget consumerTarget = requireConsumerTarget();

        final ConsumerRecord[] consumerRecords;
        try {
            consumerRecords = consumerTarget.path("/topics/" + topic)
                    .request("application/vnd.kafka.binary.v1+json")
                    .get(ConsumerRecord[].class);
        } catch (ClientErrorException ex) {
            if (ex.getResponse().getStatusInfo() == Response.Status.NOT_FOUND) {
                throw new ConsumerException("Consumer endpoint not found. Does '" + topic + "' topic exist?", ex);
            } else {
                throw new ConsumerException("Unexpected client error", ex);
            }
        }

        return Arrays.stream(consumerRecords).collect(toList());
    }

    /**
     * Provide message iterator.
     *
     * @param topic topic
     * @return iterator emitting consumed messages
     */
    public Iterator<ConsumerRecord> consume(String topic) {
        return stream(topic).iterator();
    }

    /**
     * Provide message stream.
     *
     * @param topic topic
     * @return stream emitting consumed messages
     */
    public Stream<ConsumerRecord> stream(String topic) {
        final PollingSupplier<Collection<ConsumerRecord>> supplier = createFetchPollingSupplier(topic);

        final SupplierIterator<Optional<Collection<ConsumerRecord>>> supplierIterator =
                new SupplierIterator<>(() -> supplier.get().filter(m -> !m.isEmpty()), Optional::isPresent);

        return Iterators.asStream(supplierIterator)
                .map(Optional::get)
                .flatMap(Collection::stream);
    }

    public void shutdown() {
        final WebTarget consumerTarget = requireConsumerTarget();
        consumerTarget.request().delete();
        setConsumerTarget(null);
    }

    private void setConsumerTarget(WebTarget consumerTarget) {
        consumerTargetLocker.call(() -> this.consumerTarget.set(consumerTarget));
    }

    private WebTarget requireConsumerTarget() {
        final WebTarget consumerTarget = consumerTargetLocker.get(this.consumerTarget::get);
        if (consumerTarget == null) {
            throw new IllegalStateException("Consumer not started");
        }
        return consumerTarget;
    }

}
