import ballerina/io;
import ballerina/kafka;
import ballerina/lang.'string as strings;
import ballerina/log;

// `bootstrapServers` is the list of remote server endpoints of the Kafka brokers.
kafka:ConsumerConfig consumerConfigs = {
    bootstrapServers: "localhost:9092",
    groupId: "group-id",
    topics: ["test-kafka-topic"],
    pollingIntervalInMillis: 1000,
    autoCommit: false
};

listener kafka:Consumer consumer = new (consumerConfigs);

service kafkaService on consumer {

    resource function onMessage(kafka:Consumer kafkaConsumer,
            kafka:ConsumerRecord[] records) {
        // The set of Kafka records dispatched to the service processed one by one.
        foreach var kafkaRecord in records {
            processKafkaRecord(kafkaRecord);
        }
        // Commit offsets returned for returned records by marking them as consumed.
        var commitResult = kafkaConsumer->commit();
        if (commitResult is error) {
            log:printError("Error occurred while committing the " +
                    "offsets for the consumer ", commitResult);
        }
    }
}

function processKafkaRecord(kafka:ConsumerRecord kafkaRecord) {
    byte[] serializedMsg = kafkaRecord.value;
    string|error msg = strings:fromBytes(serializedMsg);
    if (msg is string) {
        // Print the retrieved Kafka record.
        io:println("Topic: ", kafkaRecord.topic, " Partition: ",
            kafkaRecord.partition.toString(), " Received Message: ", msg);
    } else {
        log:printError("Error occurred while converting message data", msg);
    }
}
