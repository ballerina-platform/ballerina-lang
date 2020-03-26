import ballerina/kafka;
import ballerina/log;

kafka:ConsumerConfiguration consumerConfigs = {
    // `bootstrapServers` is the list of remote server endpoints of the Kafka
    // brokers.
    bootstrapServers: "localhost:9092",
    groupId: "group-id",
    // Subscribes to the topic "test-kafka-topic.
    topics: ["test-kafka-topic"],
    pollingIntervalInMillis: 1000,
    // Using default int deserializer for the keys.
    keyDeserializerType: kafka:DES_INT,
    // Using default string deserializer for the values.
    valueDeserializerType: kafka:DES_STRING,
    // Set auto.commit to false, so the records should be manually committed.
    autoCommit: false
};

listener kafka:Consumer consumer = new (consumerConfigs);

service kafkaService on consumer {
    // This resource will be executed when a message is published to the
    // subscribed topic / topics.
    resource function onMessage(kafka:Consumer kafkaConsumer,
            kafka:ConsumerRecord[] records) {
        // The set of Kafka records dispatched to the service processed one by
        // one.
        foreach var kafkaRecord in records {
            processKafkaRecord(kafkaRecord);
        }
        // Commit offsets returned for returned records by marking them as
        // consumed.
        var commitResult = kafkaConsumer->commit();
        if (commitResult is error) {
            log:printError("Error occurred while committing the " +
                "offsets for the consumer ", commitResult);
        }
    }
}

function processKafkaRecord(kafka:ConsumerRecord kafkaRecord) {
    anydata value = kafkaRecord.value;
    anydata key = kafkaRecord.key;
    // Value should be a `string`, since we use string deserializer sor value.
    if (value is string) {
        // Key should be an `int`, since we use int deserializer for key.
        if (key is int) {
            // Print the received kafka record
            log:printInfo("Topic: " + kafkaRecord.topic);
            log:printInfo("Partition: " + kafkaRecord.partition.toString());
            log:printInfo("Key: " + key.toString());
            log:printInfo("Value: " + value);
        } else {
            log:printError("Invalid key type received");
        }
    } else {
        log:printError("Invalid value type received");
    }
}
