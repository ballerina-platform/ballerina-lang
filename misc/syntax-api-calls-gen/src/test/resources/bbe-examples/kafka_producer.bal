import ballerina/io;
import ballerinax/kafka;

kafka:ProducerConfiguration producerConfiguration = {
    // The `bootstrapServers` is the list of remote server endpoints of the
    // Kafka brokers.
    bootstrapServers: "localhost:9092",

    clientId: "basic-producer",
    acks: "all",
    retryCount: 3,
    // Uses the builtin string serializer for the values.
    valueSerializerType: kafka:SER_STRING,
    // Uses the builtin int serializer for the keys.
    keySerializerType: kafka:SER_INT

};

kafka:Producer kafkaProducer = new (producerConfiguration);

public function main() {
    string message = "Hello World, Ballerina";
    // Send the message to the Kafka topic
    var sendResult = kafkaProducer->send(message, "test-kafka-topic", key = 1);

    if (sendResult is error) {
        io:println("Error occurred while sending data: " +
                    sendResult.toString());
    } else {
        io:println("Message sent successfully.");
    }
    // Flush the sent messages
    var flushResult = kafkaProducer->flushRecords();

    if (flushResult is error) {
        io:println("Error occurred while flushing the data: " +
                    flushResult.toString());
    } else {
        io:println("Records were flushed successfully.");
    }
}
