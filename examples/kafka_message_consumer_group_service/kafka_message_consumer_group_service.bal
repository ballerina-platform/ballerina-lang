import ballerina/kafka;
import ballerina/log;

kafka:ConsumerConfiguration consumerConfigs = {
    // `bootstrapServers` is the list of remote server endpoints of the Kafka
    // brokers.
    bootstrapServers: "localhost:9092",
    // Using two concurrent consumers to work as a group.
    concurrentConsumers: 2,
    groupId: "group-id",
    // Subscribes to the topic "test-kafka-topic".
    topics: ["test-kafka-topic"],
    pollingIntervalInMillis: 1000,
    // using default string deserializer to deserialize the kafka value.
    valueDeserializerType: kafka:DES_STRING
};

listener kafka:Consumer consumer = new (consumerConfigs);

service kafkaService on consumer {
    // This resource executes when a message or a set of messages published
    // to the subscribed topic / topics.
    resource function onMessage(kafka:Consumer kafkaConsumer,
        kafka:ConsumerRecord[] records) {
        // The set of Kafka records dispatched to the service processed one by one.
        foreach var kafkaRecord in records {
            processKafkaRecord(kafkaRecord);
        }
    }
}

function processKafkaRecord(kafka:ConsumerRecord kafkaRecord) {
    anydata message = kafkaRecord.value;
    if (message is string) {
        // Print the retrieved Kafka record.
        log:printInfo("Topic: " + kafkaRecord.topic + " Partition: " +
            kafkaRecord.partition.toString() + " Received Message: " + message);
    } else {
        log:printError("Error occurred while retrieving message data;" +
            "Unexpected type");
    }
}
