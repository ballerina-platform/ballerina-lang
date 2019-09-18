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

kafka:ConsumerConfig consumerConfigs = {
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
    byte[] serializedMsg = kafkaRecord.value;
    string | error msg = 'string:fromBytes(serializedMsg);
    if (msg is string) {
        // Print the retrieved Kafka record.
        io:println("Topic: ", kafkaRecord.topic, " Received Message: ", msg);
    } else {
        log:printError("Error occurred while converting message data", msg);
    }
}
````

### Kafka Producer

Following is a simple program which publishes a message to 'test-kafka-topic' topic in a remote Kafka broker cluster.

```ballerina
import ballerina/kafka;

kafka:ProducerConfig producerConfigs = {
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

>**Note:** The default thread pool size used in Ballerina is number of processors available * 2. You can configure the thread pool size by using the `BALLERINA_MAX_POOL_SIZE` environment variable.
