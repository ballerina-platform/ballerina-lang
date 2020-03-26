import ballerina/io;
import ballerina/kafka;

kafka:ProducerConfiguration producerConfiguration = {
    // `bootstrapServers` is the list of remote server endpoints of the Kafka
    // brokers.
    bootstrapServers: "localhost:9092",
    clientId: "basic-producer",
    acks: "all",
    retryCount: 3,
    // Use builtin string serializer for the values.
    valueSerializerType: kafka:SER_STRING,
    // Use builtin int serializer for the keys.
    keySerializerType: kafka:SER_INT
};

kafka:Producer kafkaProducer = new (producerConfiguration);

public function main() {
    string message = "Hello World, Ballerina";
    var sendResult = kafkaProducer->send(message, "test-kafka-topic", key = 1);
    if (sendResult is error) {
        io:println("Error occurred while sending data: " + sendResult.toString());
    } else {
        io:println("Message sent successfully");
    }
    var flushResult = kafkaProducer->flushRecords();
    if (flushResult is error) {
        io:println("Error occurred while flishing the data: " + flushResult.toString());
    } else {
        io:println("Records been flushed successfully");
    }
}
