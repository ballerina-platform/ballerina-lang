import ballerina/io;
import ballerina/kafka;
import ballerina/lang.'string as strings;
import ballerina/log;

// `bootstrapServers` is the list of remote server endpoints of the Kafka brokers.
kafka:ConsumerConfig consumerConfigs = {
    bootstrapServers: "localhost:9092",
    groupId: "group-id",
    offsetReset: "earliest",
    topics: ["test-kafka-topic"]
};

kafka:Consumer consumer = new (consumerConfigs);

public function main() {
    // Poll the consumer for messages.
    var results = consumer->poll(1000);
    if (results is error) {
        log:printError("Error occurred while polling ", results);
    } else {
        foreach var kafkaRecord in results {
            // Convert byte[] to string.
            byte[] serializedMsg = kafkaRecord.value;
            string | error msg = strings:fromBytes(serializedMsg);
            if (msg is string) {
                // Print the retrieved Kafka record.
                io:println("Topic: ", kafkaRecord.topic, " Received Message: ", msg);
            } else {
                log:printError("Error occurred while converting message data", msg);
            }
        }
    }
}
