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

    resource function onMessage(kafka:ConsumerAction consumerAction, kafka:ConsumerRecord[] records) {
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
    // Here we create a producer configs with optional parameters client.id - used for broker side logging.
    // acks - number of acknowledgments for request complete,
    // noRetries - number of retries if record send fails.
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

Please find the producer configuration parameters below:

| Parameter | Description  |
| :---   | :- |
| bootstrapServers | List of remote server endpoints of Kafka brokers. |
| acks | Number of acknowledgments. |
| compressionType | Compression type to be used for messages. |
| clientId | Identifier to be used for server side logging. |
| metricsRecordingLevel | Metrics recording level. |
| metricReporterClasses | Metrics reporter classes. |
| partitionerClass | Partitioner class to be used to select partition to which the message is sent. |
| interceptorClasses | Interceptor classes to be used before sending records. |
| transactionalId | Transactional ID to be used in transactional delivery. |
| bufferMemory | Total bytes of memory the producer can use to buffer records. |
| noRetries | Number of retries to resend a record. |
| batchSize | Number of records to be batched for a single request. Use 0 for no batching. |
| linger | Delay to allow other records to be batched. |
| sendBuffer | Size of the TCP send buffer (SO_SNDBUF). |
| receiveBuffer | Size of the TCP receive buffer (SO_RCVBUF). |
| maxRequestSize | The maximum size of a request in bytes. |
| reconnectBackoffTimeInMillis | Time to wait before attempting to reconnect. |
| reconnectBackoffMaxTimeInMillis | Maximum amount of time in milliseconds to wait when reconnecting. |
| retryBackoffTimeInMillis | Time to wait before attempting to retry a failed request. |
| maxBlock | Maximum block time which the send is blocked, when the buffer is full. |
| requestTimeoutInMillis | Wait time for response of a request. |
| metadataMaxAgeInMillis | Maximum time to force a refresh of metadata. |
| metricsSampleWindowInMillis | Time window for a metrics sample to computed over. |
| metricsNumSamples | Number of samples maintained to compute metrics. |
| maxInFlightRequestsPerConnection | Maximum number of unacknowledged requests on a single connection. |
| connectionsMaxIdleTimeInMillis | Close idle connections after the number of milliseconds. |
| transactionTimeoutInMillis | Timeout for transaction status update from the producer. |
| enableIdempotence | Exactly one copy of each message is written in the stream when enabled. |

>**Note:** The default thread pool size used in Ballerina is number of processers available * 2. You can configure the thread pool size by using the `BALLERINA_MAX_POOL_SIZE` environment variable.
