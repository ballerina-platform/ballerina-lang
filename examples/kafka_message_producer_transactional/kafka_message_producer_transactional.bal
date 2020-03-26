import ballerina/io;
import ballerina/kafka;

kafka:ProducerConfiguration producerConfigs = {
    // `bootstrapServers` is the list of remote server endpoints of the Kafka
    // brokers.
    bootstrapServers: "localhost:9092",
    clientId: "basic-producer",
    acks: "all",
    retryCount: 3,
    // `enableIdempotence` should set to `true` to make a producer
    // transactional.
    enableIdempotence: true,
    // a `transactionalId` must be provided to make a producer transactional.
    transactionalId: "test-transactional-id"
};

kafka:Producer kafkaProducer = new (producerConfigs);

public function main() {
    string message = "Hello World Transaction Message";
    byte[] serializedMessage = message.toBytes();

    // Here, we create a producer config with optional parameters.
    // transactional.id - enable transactional message production.
    kafkaAdvancedTransactionalProduce(serializedMessage);
}

function kafkaAdvancedTransactionalProduce(byte[] message) {
    transaction {
        var sendResult = kafkaProducer->send(message, "test-kafka-topic",
            partition = 0);
        if (sendResult is error) {
            io:println("Kafka producer failed to send first message",
                sendResult.toString());
        }
    } committed {
        io:println("Transaction committed");
    } aborted {
        io:println("Transaction aborted");
    }
}
