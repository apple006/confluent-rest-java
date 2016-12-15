# Description

`confluent-rest-java` is a simple rest client for Confluent Kafka Proxy.

# Usage

## Maven

Add `confluent-rest-java` dependency to your `pom.xml`.

```xml
<dependency>
    <groupId>com.hubrick</groupId>
    <artifactId>confluent-rest-java</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```

## Producer

Create producer like so:
```java
final ConfluentKafkaProducer producer =
    new ConfluentKafkaProducer("http://localhost:8082", ClientBuilder::newClient);
```

Send messages
```java
final KeyedMessage<String, String> message =
    KeyedMessage.asKeyedMessage("test", "Here we go"); // specify topic and message

producer.send(message);
```

or
```
final ProducerRecord record = new ProducerRecord();
record.setKey("c67f056b-2349-4367-8d5d-c47311fc713c".getBytes());
record.setValue("Hello there!".getBytes());
record.setPartition(0);

producer.send("test", record);
```

## Consumer

Create consumer
```java
final ConsumerConfig config = new ConsumerConfig("tests");
config.setAutoOffsetReset("largest");
config.setAutoCommitEnable(true);
config.setTimeout(5000L);

final ConfluentKafkaConsumer consumer =
    new ConfluentKafkaConsumer("http://localhost:8082", config, ClientBuilder::newClient);

consumer.start(); // consumer must be started before it can be used
```

Read messages:
```java
final Collection<ConsumerRecord> records = consumer.fetch("test");
```

or 
```java
final Iterator<ConsumerRecord> records = consumer.consume("test");
final ConsumerRecord record = records.next();
```

Always stop consumer after use:
```java
consumer.stop();
```

# Building

You will need Maven to build the library. Also, for running integration tests you will need Docker installed.  
 
    $ mvn clean package
    
Installing the library

    $ mvn clean install
    
Running tests

    $ mvn clean verify
