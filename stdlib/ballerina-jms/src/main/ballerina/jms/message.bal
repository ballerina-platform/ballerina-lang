package ballerina.jms;

public struct Message {
}

@Description { value:"Gets text content of the JMS message"}
@Return { value:"string: Text Message Content" }
public native function <Message msg> getTextMessageContent () returns (string);
