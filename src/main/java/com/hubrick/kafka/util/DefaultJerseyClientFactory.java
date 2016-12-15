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
package com.hubrick.kafka.util;

import com.hubrick.kafka.confluent.core.ClientFactory;
import org.glassfish.jersey.client.ClientConfig;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

/**
 * @author ahanin
 * @since 1.0.0
 */
public final class DefaultJerseyClientFactory implements ClientFactory {

    private static final DefaultJerseyClientFactory INSTANCE = new DefaultJerseyClientFactory();

    private DefaultJerseyClientFactory() {
    }

    public static DefaultJerseyClientFactory getInstance() {
        return INSTANCE;
    }

    @Override
    public Client createClient(ClientConfig clientConfig) {
        return ClientBuilder.newClient(clientConfig);
    }

}
