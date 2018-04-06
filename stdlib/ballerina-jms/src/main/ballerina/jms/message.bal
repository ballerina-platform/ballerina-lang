package ballerina.jms;

public type Message object {
    @Description { value:"Gets text content of the JMS message"}
    @Return { value:"string: Text Message Content" }
    public native function getTextMessageContent () returns (string);
};


