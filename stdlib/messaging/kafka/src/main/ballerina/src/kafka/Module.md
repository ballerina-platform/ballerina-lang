## Module Overview

This module is used to interact with Kafka Brokers via Kafka Consumer and Kafka Producer clients.
This module supports Kafka 1.x.x and 2.0.0 versions.

For information on the operations, which you can perform with this module, see the below **Functions**.
For examples on the usage of the operations, see the following. 
* [Producer Example](https://ballerina.io/swan-lake/learn/by-example/kafka_producer.html) 
* [Consumer Service Example](https://ballerina.io/swan-lake/learn/by-example/kafka_consumer_service.html)
* [Consumer Client Example](https://ballerina.io/swan-lake/learn/by-example/kafka_consumer_client.html)
* [Transactional Producer Example](https://ballerina.io/swan-lake/learn/by-example/kafka_producer_transactional.html)
* [Consumer with SASL Authentication Example](https://ballerina.io/swan-lake/learn/by-example/kafka-authentication-sasl-plain-consumer.html)
* [Producer with SASL Authentication Example](https://ballerina.io/swan-lake/learn/by-example/kafka-authentication-sasl-plain-producer.html)

#### Basic Usages

##### Publishing Messages

1. Initialize the Kafka message producer.
```ballerina
kafka:ProducerConfiguration producerConfiguration = {
    bootstrapServers: "localhost:9092",
    clientId: "basic-producer",
    acks: "all",
    retryCount: 3,
    valueSerializerType: kafka:SER_STRING,
    keySerializerType: kafka:SER_INT
};

kafka:Producer kafkaProducer = new (producerConfiguration);
```
2. Use the `kafka:Producer` to publish messages. 
```ballerina
string message = "Hello World, Ballerina";
kafka:ProducerError? result = kafkaProducer->send(message, "kafka-topic", key = 1);
```

##### Consuming Messages

1. Initializing the Kafka message consumer. 
```ballerina
kafka:ConsumerConfiguration consumerConfiguration = {
    bootstrapServers: "localhost:9092",
    groupId: "group-id",
    offsetReset: "earliest",
    topics: ["kafka-topic"]
};

kafka:Consumer consumer = new (consumerConfiguration);
```
2. Use the `kafka:Consumer` as a simple record consumer.
```ballerina
kafka:ConsumerRecord[]|kafka:ConsumerError result = consumer->poll(1000);
```
3. Use the `kafka:Consumer` as a listener.
```ballerina
listener kafka:Consumer consumer = new (consumerConfiguration);

service kafkaService on consumer {
    // This resource will be executed when a message is published to the
    // subscribed topic/topics.
    resource function onMessage(kafka:Consumer kafkaConsumer,
            kafka:ConsumerRecord[] records) {
    }
}
```

#### Send Data Using Avro
The Ballerina Kafka module supports Avro serialization and deserialization.

To try this, create a new Ballerina project and two modules inside it.

Execute the below commands to do this.
```shell script
ballerina new kafka_avro_sample
cd kafka_avro_sample
ballerina add producer
ballerina add consumer
```
 ##### Dependencies
 To use Avro, you need to add the necessary dependencies to the Ballerina project you created. 
 To do so, download the necessary dependencies and add them inside the `resources
 ` directory. Also, add those dependencies to the `Ballerina.toml` file of your project.
 The following is a sample `Ballerina.toml` file with the dependencies.
 
 ```toml
     [[platform.libraries]]
     module = "producer"
     path = "./resources/kafka-avro-serializer-5.4.1.jar"
     artifactId = "kafka-avro-serializer"
     version = "5.4.1"
     groupId = "io.confluent"
 
     [[platform.libraries]]
     module = "producer"
     path = "./resources/kafka-schema-registry-client-5.4.1.jar"
     artifactId = "kafka-schema-registry-client"
     version = "5.4.1"
     groupId = "io.confluent"
 
     [[platform.libraries]]
     module = "producer"
     path = "./resources/common-config-5.4.1.jar"
     artifactId = "common-config"
     version = "5.4.1"
     groupId = "io.confluent"
 
     [[platform.libraries]]
     module = "producer"
     path = "./resources/common-utils-5.4.1.jar"
     artifactId = "common-utils"
     version = "5.4.1"
     groupId = "io.confluent"
 
     [[platform.libraries]]
     module = "producer"
     path = "./resources/avro-1.9.2.jar"
     artifactId = "avro"
     version = "1.9.2"
     groupId = "org.apache.avro"
 
     [[platform.libraries]]
     module = "producer"
     path = "./resources/jackson-core-2.10.3.jar"
     artifactId = "jackson-core"
     version = "1.9.2"
     groupId = "com.fasterxml.jackson.core"
 
     [[platform.libraries]]
     module = "producer"
     path = "./resources/jackson-databind-2.10.3.jar"
     artifactId = "jackson-databind"
     version = "2.10.3"
     groupId = "com.fasterxml.jackson.core"
 
     [[platform.libraries]]
     module = "producer"
     path = "./resources/jackson-annotations-2.10.3.jar"
     artifactId = "jackson-annotations"
     version = "2.10.3"
     groupId = "com.fasterxml.jackson.core"
```

Now, the directory structure will look like below (some of the files are ignored).

```shell script
├── Ballerina.toml
├── resources
│   ├── avro-1.9.2.jar
│   ├── common-config-5.4.1.jar
│   ├── common-utils-5.4.1.jar
│   ├── jackson-annotations-2.10.3.jar
│   ├── jackson-core-2.10.3.jar
│   ├── jackson-databind-2.10.3.jar
│   ├── kafka-avro-serializer-5.4.1.jar
│   └── kafka-schema-registry-client-5.4.1.jar
└── src
    ├── consumer
    │   └── main.bal
    └── producer
        └── main.bal
```

##### Avro Producer
The `kafka:Proucer` can be configured to send data using Avro by providing the following configurations.

 - `schemaString`: This is the schema string, which is used to define the Avro schema.
 - `dataRecord`: The data record you want to send through Kafka.

src/producer/main.bal:
```ballerina
import ballerina/io;
import ballerina/kafka;

public type Person record {
    string name;
    int age;
};

kafka:ProducerConfiguration configs = {
    bootstrapServers: "<KAFKA_BROKER_HOST_AND_PORT>",
    // Other configurations
    valueSerializerType: kafka:SER_AVRO,
    schemaRegistryUrl: "<SCHEMA_REGISTRY_URL>"
};

string schema = "{\"type\" : \"record\"," +
                  "\"namespace\" : \"Thisaru\"," +
                  "\"name\" : \"person\"," +
                  "\"fields\" : [" + 
                    "{ \"name\" : \"name\", \"type\" : \"string\" }," +
                    "{ \"name\" : \"age\", \"type\" : \"int\" }" +
                  "]}";

public function main() {
    kafka:Producer producer = new(configs);

    Person person = {
        name: "Lahiru Perera",
        age: 28
    };

    kafka:AvroRecord avroRecord = {
        schemaString: schema,
        dataRecord: person
    };

    kafka:ProducerError? result = producer->send(avroRecord, "add-person");
    if (result is kafka:ProducerError) {
        io:println(result);    
    }
}
```

##### Avro Consumer
The Kafka implementation of Ballerina currently supports Avro deserialization only for generic records.
The Consumer will return a `kafka:AvroGenericRecord` with the data received from Avro.

The following is a sample consumer.

src/producer/main.bal:
```ballerina
import ballerina/io;
import ballerina/kafka;

kafka:ConsumerConfiguration configs = {
    bootstrapServers: "<KAFKA_BROKER_HOST_AND_PORT>",
    groupId: "test-group",
    // Other configurations
    topics: ["add-person"],
    valueDeserializerType: kafka:DES_AVRO,
    schemaRegistryUrl: "<SCHEMA_REGISTRY_URL>"
};

listener kafka:Consumer consumer = new(configs);

service KafkaService on consumer {
    resource function onMessage(kafka:Consumer consumer, kafka:ConsumerRecord[] records) {
        io:println("Records received");
        foreach var kafkaRecord in records {
            anydata value = kafkaRecord.value;
            if (value is kafka:AvroGenericRecord) {
                io:println(value);
            } else {
                io:println("Invalid record type");
            }
        }
    }
}
```

Now, execute the below command to run the consumer:
```shell script
ballerina run consumer
```
This will start the Kafka service to listen. You can verify it by the following messages, which will be displayed on the screen.

```shell script
[ballerina/kafka] kafka servers: <KAFKA_BROKER_HOST_AND_PORT>
[ballerina/kafka] subscribed topics: add-person
[ballerina/kafka] started kafka listener
```

Then, open another terminal and execute the below command to run the producer:
```shell script
ballerina run producer
```

Now, the consumer will receive the data and the received data will be printed on the Console as follows.

```shell script
Records received
name=Lahiru Perera age=28
```
