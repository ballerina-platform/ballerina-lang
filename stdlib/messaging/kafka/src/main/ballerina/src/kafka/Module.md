## Package overview

Ballerina Kafka Connector is used to connect Ballerina with Kafka Brokers. With the Kafka Connector, Ballerina can act as Kafka Consumers and Kafka Producers.
This connector supports kafka 1.x.x and 2.0.0 versions.

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

Please find the consumer parameters below:

| Parameter | Description  |
| :---   | :- |
| bootstrapServers | The list of host and port pairs, which are the addresses of the Kafka brokers in a "bootstrap" Kafka cluster. |
| groupId | The Identifier of the consumer group. |
| topics | The topics that must be listened by this consumer. |
| pollingIntervalInMillis | The time interval that a consumer polls the topic. |
| offsetReset | Offset reset strategy if no initial offset. |
| partitionAssignmentStrategy | Strategy class for handling the partition assignment among consumers. |
| metricsRecordingLevel | Metrics recording level. |
| metricsReporterClasses | Metrics reporter classes. |
| clientId | Identifier to be used for server side logging. |
| interceptorClasses | Interceptor classes to be used before sending records. |
| isolationLevel | Transactional message reading method. Use "read_committed" to read committed messages only in transactional mode when poll() is called. Use "read_uncommitted" to read all the messages, even the aborted ones. |
| properties | Additional properties if required. |
| sessionTimeoutInMillis | Timeout used to detect consumer failures when heartbeat threshold is reached. |
| heartBeatIntervalInMillis | Expected time between heartbeats. |
| metadataMaxAgeInMillis | Maximum time to force a refresh of metadata. |
| autoCommitIntervalInMillis | Auto committing interval for commit offset, when auto-commit is enabled. |
| maxPartitionFetchBytes | The maximum amount of data per-partition the server returns. |
| sendBuffer | Size of the TCP send buffer (SO_SNDBUF). |
| receiveBuffer | Size of the TCP receive buffer (SO_RCVBUF). |
| fetchMinBytes | Minimum amount of data the server should return for a fetch request. |
| fetchMaxBytes | Maximum amount of data the server should return for a fetch request. |
| fetchMaxWaitTimeInMillis | Maximum amount of time the server will block before answering the fetch request. |
| reconnectBackoffTimeMaxInMillis | Maximum amount of time in milliseconds to wait when reconnecting. |
| retryBackoffInMillis | Time to wait before attempting to retry a failed request. |
| metricsSampleWindowInMillis | Window of time a metrics sample is computed over. |
| metricsNumSamples | Number of samples maintained to compute metrics. |
| requestTimeoutInMillis | Wait time for response of a request. |
| connectionMaxIdleTimeInMillis | Close idle connections after the number of milliseconds. |
| maxPollRecords | Maximum number of records returned in a single call to poll. |
| maxPollInterval | Maximum delay between invocations of poll. |
| reconnectBackoffTimeInMillis | Time to wait before attempting to reconnect. |
| pollingTimeoutInMillis | Timeout interval for polling. |
| concurrentConsumers | Number of concurrent consumers. |
| defaultApiTimeoutInMillis | Default API timeout value for APIs with duration. |
| autoCommit | Enables auto committing offsets. |
| checkCRCS | Check the CRC32 of the records consumed. |
| excludeInternalTopics | Whether records from internal topics should be exposed to the consumer. |
| decoupleProcessing | Decouples processing. |

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
````

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
