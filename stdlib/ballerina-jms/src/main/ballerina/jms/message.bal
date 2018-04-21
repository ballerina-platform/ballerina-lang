
public type Message object {
    documentation {Gets text content of the JMS message
        returns message content as string}
    public native function getTextMessageContent() returns @tainted string|error;

    documentation {Sets a JMS transport string property from the message
        P{{key}} The string property name
        P{{value}} The string property value}
    public native function setStringProperty(@sensitive string key, string value) returns error?;

    documentation {Gets a JMS transport string property from the message
        P{{key}} The string property name
        returns The string property value}
    public native function getStringProperty(@sensitive string key) returns @tainted string|()|error;

    documentation {Sets a JMS transport integer property from the message
        P{{key}} The integer property name
        P{{value}} The integer property value}
    public native function setIntProperty(@sensitive string key, int value) returns error?;

    documentation {Gets a JMS transport integer property from the message
        P{{key}} The integer property name
        returns The integer property value}
    public native function getIntProperty(@sensitive string key) returns @tainted int|error;

    documentation {Sets a JMS transport boolean property from the message
        P{{key}} The boolean property name
        P{{value}} The boolean property value}
    public native function setBooleanProperty(@sensitive string key, boolean value) returns error?;

    documentation {Gets a JMS transport boolean property from the message
        P{{key}} The boolean property name
        returns The boolean property value}
    public native function getBooleanProperty(@sensitive string key) returns @tainted boolean|error;

    documentation {Sets a JMS transport float property from the message
        P{{key}} The float property name
        P{{value}} The float property value}
    public native function setFloatProperty(@sensitive string key, float value) returns error?;

    documentation {Gets a JMS transport float property from the message
        P{{key}} The float property name
        returns The float property value}
    public native function getFloatProperty(@sensitive string key) returns @tainted float|error;

    documentation {Get JMS transport header MessageID from the message
        returns The header value}
    public native function getMessageID() returns @tainted string|error;

    documentation {Get JMS transport header Timestamp from the message
        returns The header value}
    public native function getTimestamp() returns @tainted int|error;

    documentation {Sets DeliveryMode JMS transport header to the message
        P{{mode}} The header value}
    public native function setDeliveryMode(@sensitive int mode) returns error?;

    documentation {Get JMS transport header DeliveryMode from the message
        returns The header value" }
    public native function getDeliveryMode() returns @tainted int|error;

    documentation {Sets Expiration JMS transport header to the message
        P{{value}} The header value}
    public native function setExpiration(@sensitive int value) returns error?;

    documentation {Get JMS transport header Expiration from the message
        returns int: The header value}
    public native function getExpiration() returns @tainted int|error;

    documentation { Sets Type JMS transport header to the message
        P{{messageType}} The message type header value
        returns error if any JMS provider level internal error occur}
    public native function setType(@sensitive string messageType) returns error?;

    documentation { Get JMS transport header Type from the message
        returns The message type header value }
    public native function getType() returns @tainted string|error;

    documentation {Clear JMS properties of the message
        returns error if any JMS provider level internal error occur}
    public native function clearProperties();

    documentation {Clears body of the JMS message
        returns error if any JMS provider level internal error occur}
    public native function clearBody() returns error?;

    documentation {Sets Priority JMS transport header to the message
        P{{value}} The header value}
    public native function setPriority(@sensitive int value) returns error?;

    documentation {Get JMS transport header Priority from the message
        returns The header value}
    public native function getPriority() returns @tainted int|error;

    documentation {Get JMS transport header Redelivered from the message
        returns The header value}
    public native function getRedelivered() returns @tainted boolean|error;

    documentation {Sets CorrelationID JMS transport header to the message
        P{{value}} The header value}
    public native function setCorrelationID(@sensitive string value) returns error?;

    documentation {Get JMS transport header CorrelationID from the message
        returns The header value}
    public native function getCorrelationID() returns @tainted string|()|error;
};
