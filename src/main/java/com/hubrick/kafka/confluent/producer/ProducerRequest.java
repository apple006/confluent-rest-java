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

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author ahanin
 * @since 1.0.0
 */
public class ProducerRequest {

    private Collection<ProducerRecord> records;

    public ProducerRequest(Collection<ProducerRecord> records) {
        this.records = new ArrayList<>(records);
    }

    public Collection<ProducerRecord> getRecords() {
        return records;
    }

    public void setRecords(Collection<ProducerRecord> records) {
        this.records = records;
    }
}
