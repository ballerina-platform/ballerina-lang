import ballerina/kafka;
import ballerina/log;

kafka:ProducerConfiguration producerConfigs = {
    // Here, we create the configs of a producer with optional parameters.
    // client.id - used for broker-side logging.
    // `acks` - number of acknowledgments to complete the request.
    // `noRetries` - number of retries if record sending fails.
    // `bootstrapServers` is the list of remote server endpoints of
    // the Kafka brokers
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
