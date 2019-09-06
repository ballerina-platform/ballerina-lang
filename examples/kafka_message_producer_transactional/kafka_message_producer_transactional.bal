import ballerina/io;
import ballerina/kafka;
import ballerina/log;

kafka:ProducerConfig producerConfigs = {
    // Here we create a producer configs with optional parameters client.id - used for broker side logging.
    // `acks` - number of acknowledgments for request complete,
    // `noRetries` - number of retries if record send fails.
    // `bootstrapServers` is the list of remote server endpoints of the Kafka brokers
    bootstrapServers: "localhost:9092",
    clientId: "basic-producer",
    acks: "all",
    retryCount: 3,
    enableIdempotence: true,
    transactionalId: "test-transactional-id"
};

kafka:Producer kafkaProducer = new (producerConfigs);

public function main() {
    string msg1 = "Hello World Transaction Message";
    byte[] serializedMsg = msg1.toBytes();

    // Here we create a producer configs with optional parameter transactional.id - enable transactional message production.
    kafkaAdvancedTransactionalProduce(serializedMsg);
}

function kafkaAdvancedTransactionalProduce(byte[] msg) {
    // Kafka transactions allows messages to be send multiple partition atomically on KafkaProducerClient. Kafka Local transactions can only be used
    // when you are sending multiple messages using the same KafkaProducerClient instance.
    transaction {
        var sendResult = kafkaProducer->send(msg, "test-kafka-topic", partition = 0);
        if (sendResult is error) {
            log:printError("Kafka producer failed to send first message", sendResult);
        }
    } committed {
        io:println("Transaction committed");
    } aborted {
        io:println("Transaction aborted");
    }
}
