import ballerina/jms;
import ballerina/log;

// This initializes a JMS connection with the provider.
jms:Connection jmsConnection = new({
        initialContextFactory: "bmbInitialContextFactory",
        providerUrl: "amqp://admin:admin@carbon/carbon"
            + "?brokerlist='tcp://localhost:5672'"
    });

// This initializes a JMS session on top of the created connection.
jms:Session jmsSession = new(jmsConnection, {
        acknowledgementMode: "AUTO_ACKNOWLEDGE"
    });

// This initializes a queue receiver on top of the created sessions.
listener jms:QueueReceiver queueReceiver = new(jmsSession, queueName = "MyQueue");

public function main() {
    jms:QueueReceiverCaller caller = queueReceiver.getCallerActions();
    // This keeps the JMS session alive until the message is received by the JMS provider.
    // If the message is not received within five seconds, the session times out.
    var result = caller->receive(timeoutInMilliSeconds = 5000);

    if (result is jms:Message) {
        // This is executed if the message is received.
        var messageText = result.getTextMessageContent();
        if (messageText is string) {
            log:printInfo("Message : " + messageText);
        } else {
            log:printError("Error occurred while reading message.",
                err = messageText);
        }
    } else if (result is ()) {
        // This is executed if the message is not received within five seconds.
        log:printInfo("Message not received");

    } else {
        // This is executed if an error occurs.
        log:printInfo("Error receiving message : " +
                <string>result.detail().message);
    }
}
