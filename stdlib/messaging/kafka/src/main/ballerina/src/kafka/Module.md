## Module overview

This module is used to interact with Kafka Brokers via Kafka Consumer and Kafka Producer clients.
This module supports kafka 1.x.x and 2.0.0 versions.

## Samples
### Simple Kafka Consumer

Following is a simple service which is subscribed to a topic 'test-kafka-topic' on remote Kafka broker cluster.

```ballerina
import ballerina/io;
import ballerina/kafka;
import ballerina/lang. 'string;

kafka:ConsumerConfiguration consumerConfigs = {
    bootstrapServers:"localhost:9092",
    groupId:"group-id",
    topics:["test-kafka-topic"],
    pollingIntervalInMillis:1000
};

listener kafka:Consumer consumer = new(consumerConfigs);

service kafkaService on consumer {

    resource function onMessage(kafka:ConsumerAction consumerAction,
                  kafka:ConsumerRecord[] records) {
        // Dispatched set of Kafka records to service, We process each one by one.
        foreach var kafkaRecord in records {
            processKafkaRecord(kafkaRecord);
        }
    }
}

function processKafkaRecord(kafka:ConsumerRecord kafkaRecord) {
    var value = kafkaRecord.value;
    if (value is byte[]) {
        string | error msg = 'string:fromBytes(serializedMsg);
        if (msg is string) {
            // Print the retrieved Kafka record.
            io:println("Topic: ", kafkaRecord.topic, " Received Message: ", msg);
        } else {
            log:printError("Error occurred while converting message data", msg);
        }
    }
}
````

### Kafka Producer

Following is a simple program which publishes a message to 'test-kafka-topic' topic in a remote Kafka broker cluster.

```ballerina
import ballerina/kafka;

kafka:ProducerConfiguration producerConfigs = {
    // Here we create a producer configs with optional parameters 
    // client.id - used for broker side logging.
    // acks - number of acknowledgments for request complete,
    // retryCount - number of retries if record send fails.
    bootstrapServers: "localhost:9092",
    clientId:"basic-producer",
    acks:"all",
    retryCount:3
};

kafka:Producer kafkaProducer = new(producerConfigs);

function main () {
    string msg = "Hello World, Ballerina";
    byte[] serializedMsg = msg.toByteArray("UTF-8");
    var sendResult = kafkaProducer->send(serializedMsg, "test-kafka-topic");
    if (sendResult is error) {
        log:printError("Kafka producer failed to send data", err = sendResult);
    }
}
```

### Send Data Using Avro
The Ballerina Kafka module supports Avro serialization and deserialization.

To try this, let's create a new Ballerina project and two modules inside it.

Execute the below commands to do this.
```shell script
ballerina new kafka_avro_sample
cd kafka_avro_sample
ballerina add producer
ballerina add consumer
```

 #### Dependencies
 To use Avro, you need to add the necessary dependencies to the Ballerina project you created. 
To do so, download the necessary dependencies and put them inside the `resources
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

Now, the directory structure will look like follows. (Some of the files ignored)

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

#### Avro Producer
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

#### Avro Consumer
The Kafka implementation of Ballerina currently supports Avro deserialization only for generic records.
The Consumer will return `kafka:AvroGenericRecord` with the data received from Avro.

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
