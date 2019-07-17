import ballerinax/jms;

// Create a queue sender
jms:QueueSender queueSender = new({
    initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
    providerUrl: "tcp://localhost:61616"
}, "MyPropQueue");

public function sendTextMessage () {
    // Create a Text message.
    var msg = queueSender.session.createTextMessage("Test Text");
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
         checkpanic queueSender->send(msg);
    } else {
         panic msg;
    }
}
