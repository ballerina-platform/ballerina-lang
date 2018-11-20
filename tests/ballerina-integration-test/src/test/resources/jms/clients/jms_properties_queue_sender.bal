import ballerina/jms;
import ballerina/io;

// Create a queue sender
endpoint jms:SimpleQueueSender queueSender {
    initialContextFactory: "bmbInitialContextFactory",
    providerUrl: "amqp://admin:admin@carbon/carbon?brokerlist='tcp://localhost:5772'",
    queueName: "MyPropQueue"
};

public function main () {
    // Create a Text message.
    var msg = queueSender.createTextMessage("Test Text");
    if (msg is jms:Message) {
         var returnVal = msg.setBooleanProperty("booleanProp", false);
         if (returnVal is error) {
              panic returnVal;
         }
         returnVal = msg.setIntProperty("intProp", 10);
         if (returnVal is error) {
              panic returnVal;
         }
         returnVal = msg.setFloatProperty("floatProp", 10.5);
         if (returnVal is error) {
              panic returnVal;
         }
         returnVal = msg.setStringProperty("stringProp", "TestString");
         if (returnVal is error) {
              panic returnVal;
         }
         // Send the Ballerina message to the JMS provider.
         _ = queueSender -> send(msg);
    } else {
         panic msg;
    }

    io:println("Message successfully sent by jms:SimpleQueueSender");
}
