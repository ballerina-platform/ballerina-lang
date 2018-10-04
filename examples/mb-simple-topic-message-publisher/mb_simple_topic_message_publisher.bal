import ballerina/log;
import ballerina/mb;

// Create a topic publisher.
endpoint mb:SimpleTopicPublisher publisher {
    host: "localhost",
    port: 5672,
    topicPattern: "BallerinaTopic"
};


public function main() {
    // Create a Text message.
    match (publisher.createTextMessage("Hello from Ballerina")) {
        error e => {
            log:printError("Error occurred while creating message", err = e);
        }

        mb:Message msg => {
            // Send the Ballerina message to the JMS provider.
            publisher->send(msg) but {
               error e => log:printError("Error occurred while sending message",
                                         err = e)
            };
        }
    }
}
