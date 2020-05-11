import ballerina/io;
import ballerina/kafka;

// The `kafka:AuthenticationConfiguration` is used to provide authentication-related details.
kafka:AuthenticationConfiguration authConfig = {
    // Provide the authentication mechanism used by the Kafka server.
    mechanism: kafka:AUTH_SASL_PLAIN,
    // Username and password should be set here in order to authenticate the producer.
    // Check Ballerina `config` APIs to see how to use encrypted values instead of plain text values here.
    username: "ballerina",
    password: "ballerina-secret"
};

kafka:ProducerConfiguration producerConfigs = {
    bootstrapServers: "localhost:9092",
    valueSerializerType: kafka:SER_STRING,
    // Provide the relevant authentication configuration record to authenticate the producer.
    authenticationConfiguration: authConfig
};

kafka:Producer kafkaProducer = new (producerConfigs);

public function main() {
    var result = kafkaProducer->send("Hello from Ballerina", "topic-sasl");
    if (result is error) {
        io:println(result);
    } else {
        io:println("success");
    }
}
