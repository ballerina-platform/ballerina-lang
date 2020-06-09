import ballerina/kafka;
import ballerina/log;

// The `kafka:AuthenticationConfiguration` is used to provide authentication-related details.
kafka:AuthenticationConfiguration authConfig = {
    // Provide the authentication mechanism used by the Kafka server.
    mechanism: kafka:AUTH_SASL_PLAIN,
    // Username and password should be set here in order to authenticate the consumer.
    // Check Ballerina `config` APIs to see how to use encrypted values instead of plain text values here.
    username: "ballerina",
    password: "ballerina-secret"
};

kafka:ConsumerConfiguration consumerConfig = {
    bootstrapServers:"localhost:9092",
    groupId:"test-group",
    clientId: "sasl-consumer",
    offsetReset:"earliest",
    topics:["topic-sasl"],
    valueDeserializerType: kafka:DES_STRING,
    // Provide the relevant authentication configuration record to authenticate the consumer.
    authenticationConfiguration: authConfig
};

listener kafka:Consumer consumer = new(consumerConfig);

service KafkaService on consumer {
    resource function onMessage(kafka:Consumer consumer, kafka:ConsumerRecord[] records) {
        foreach var consumerRecord in records {
            string value = <string> consumerRecord.value;
            log:printInfo(value);
        }
    }
}
