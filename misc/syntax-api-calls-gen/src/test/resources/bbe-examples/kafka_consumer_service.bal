import ballerinax/kafka;
import ballerina/log;

kafka:ConsumerConfiguration consumerConfigs = {
    // The `bootstrapServers` is the list of remote server endpoints of the
    // Kafka brokers.
    bootstrapServers: "localhost:9092",

    groupId: "group-id",
    // Subscribes to the topic `test-kafka-topic`.
    topics: ["test-kafka-topic"],

    pollingIntervalInMillis: 1000,
    // Uses the default int deserializer for the keys.
    keyDeserializerType: kafka:DES_INT,
    // Uses the default string deserializer for the values.
    valueDeserializerType: kafka:DES_STRING,
    // Set `autoCommit` to false, so that the records should be committed
    // manually.
    autoCommit: false

};

listener kafka:Consumer consumer = new (consumerConfigs);

service kafkaService on consumer {
    // This resource will be executed when a message is published to the
    // subscribed topic/topics.
    resource function onMessage(kafka:Consumer kafkaConsumer,
            kafka:ConsumerRecord[] records) {
        // The set of Kafka records dispatched to the service are processed one
        // by one.
        foreach var kafkaRecord in records {
            processKafkaRecord(kafkaRecord);
        }
        // Commit offsets for returned records by marking them as consumed.
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
    // The value should be a `string`, since the string deserializer is used
    // for the value.
    if (value is string) {
        // The key should be an `int`, since the int deserializer is used for
        // the key.
        if (key is int) {
            // Prints the received Kafka record.
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
