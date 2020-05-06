import ballerina/io;
import ballerina/kafka;
import ballerina/lang.'string;
import ballerina/log;

kafka:ConsumerConfiguration consumerConfiguration = {
    // The `bootstrapServers` is the list of remote server endpoints of the
    // Kafka brokers.
    bootstrapServers: "localhost:9092",
    groupId: "group-id",
    offsetReset: "earliest",
    // Subscribes to the topic `test-kafka-topic`.
    topics: ["test-kafka-topic"]
};

kafka:Consumer consumer = new (consumerConfiguration);

public function main() {
    // Poll the consumer for messages.
    var results = consumer->poll(1000);
    if (results is error) {
        log:printError("Error occurred while polling ", results);
    }
    kafka:ConsumerRecord[] records = <kafka:ConsumerRecord[]>results;
    foreach var kafkaRecord in records {
        anydata serializedMessage = kafkaRecord.value;
        if (serializedMessage is byte[]) {
            // Converts byte[] to string.
            string|error message = 'string:fromBytes(serializedMessage);
            if (message is string) {
                // Prints the retrieved Kafka record.
                io:println("Topic: ", kafkaRecord.topic, " Received Message: ",
                    message);
            } else {
                log:printError("Error occurred while converting message data",
                    message);
            }
        } else {
            log:printError("Error occurred while retrieving message data;" +
                "Unexpected type");
        }
    }
}
