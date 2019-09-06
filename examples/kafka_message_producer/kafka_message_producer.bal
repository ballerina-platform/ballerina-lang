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
    retryCount: 3
};

kafka:Producer kafkaProducer = new (producerConfigs);

public function main() {
    string msg = "Hello World, Ballerina";
    byte[] serializedMsg = msg.toBytes();
    var sendResult = kafkaProducer->send(serializedMsg, "test-kafka-topic");
    if (sendResult is error) {
        log:printError("Kafka producer failed to send data", sendResult);
    }
    var flushResult = kafkaProducer->flushRecords();
    if (flushResult is error) {
        log:printError("Kafka producer failed to flush the records", flushResult);
    }
}
