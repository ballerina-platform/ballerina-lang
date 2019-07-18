import ballerinax/java.jms;

jms:QueueSender queueSender = new({
    initialContextFactory: "org.apache.activemq.artemis.jndi.ActiveMQInitialContextFactory",
    providerUrl: "tcp://localhost:61616"
}, "MyPropQueue");

public function sendTextMessage () {
    jms:Message msg = checkpanic new jms:Message(queueSender.session, jms:TEXT_MESSAGE);
    checkpanic msg.setPayload("Test Text");
    var returnVal = msg.setProperty("booleanProp", false);
    if (returnVal is error) {
      panic returnVal;
    }
    returnVal = msg.setProperty("intProp", 10);
    if (returnVal is error) {
      panic returnVal;
    }
    returnVal = msg.setProperty("floatProp", 10.5);
    if (returnVal is error) {
      panic returnVal;
    }
    returnVal = msg.setProperty("stringProp", "TestString");
    if (returnVal is error) {
      panic returnVal;
    }
    checkpanic queueSender->send(msg);
}
