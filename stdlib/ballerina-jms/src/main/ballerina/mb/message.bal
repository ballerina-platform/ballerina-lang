
import ballerina/jms;

public type Message object {
    private {
        jms:Message message;
    }

    public new (message) {
    }

    documentation {Gets the internal JMS message
        returns JMS message}
    function getJMSMessage() returns @tainted jms:Message {
        return self.message;
    }

    documentation {Gets text content of the JMS message
        returns message content as string}
    public function getTextMessageContent() returns @tainted string|error {
        return self.message.getTextMessageContent();
    }

    documentation {Sets a JMS transport string property from the message
        P{{key}} The string property name
        P{{value}} The string property value}
    public function setStringProperty(@sensitive string key, string value) returns error? {
        return self.message.setStringProperty(key, value);
    }

    documentation {Gets a JMS transport string property from the message
        P{{key}} The string property name
        returns The string property value}
    public function getStringProperty(@sensitive string key) returns @tainted string|error|() {
        return self.message.getStringProperty(key);
    }

    documentation {Sets a JMS transport integer property from the message
        P{{key}} The integer property name
        P{{value}} The integer property value}
    public function setIntProperty(@sensitive string key, int value) returns error? {
        return self.message.setIntProperty(key, value);
    }

    documentation {Gets a JMS transport integer property from the message
        P{{key}} The integer property name
        returns The integer property value}
    public function getIntProperty(@sensitive string key) returns @tainted int|error {
        return self.message.getIntProperty(key);
    }

    documentation {Sets a JMS transport boolean property from the message
        P{{key}} The boolean property name
        P{{value}} The boolean property value}
    public function setBooleanProperty(@sensitive string key, boolean value) returns error? {
        return self.message.setBooleanProperty(key, value);
    }

    documentation {Gets a JMS transport boolean property from the message
        P{{key}} The boolean property name
        returns The boolean property value}
    public function getBooleanProperty(@sensitive string key) returns @tainted boolean|error {
        return self.message.getBooleanProperty(key);
    }

    documentation {Sets a JMS transport float property from the message
        P{{key}} The float property name
        P{{value}} The float property value}
    public function setFloatProperty (@sensitive string key, float value) returns error? {
        return self.message.setFloatProperty(key, value);
    }

    documentation {Gets a JMS transport float property from the message
        P{{key}} The float property name
        returns The float property value}
    public function getFloatProperty (@sensitive string key) returns @tainted float|error {
        return self.message.getFloatProperty(key);
    }

    documentation {Get JMS transport header MessageID from the message
        returns The header value}
    public function getMessageID () returns @tainted string|error {
        return self.message.getMessageID();
    }

    documentation {Get JMS transport header Timestamp from the message
        returns The header value}
    public function getTimestamp () returns @tainted int|error {
        return self.message.getTimestamp();
    }

    documentation {Sets DeliveryMode JMS transport header to the message
        P{{mode}} The header value}
    public function setDeliveryMode (@sensitive int mode) returns error? {
        return self.message.setDeliveryMode(mode);
    }

    documentation {Get JMS transport header DeliveryMode from the message
        returns The header value" }
    public function getDeliveryMode () returns @tainted int|error {
        return self.message.getDeliveryMode();
    }

    documentation {Sets Expiration JMS transport header to the message
        P{{value}} The header value}
    public function setExpiration (@sensitive int value) returns error? {
        return self.message.setExpiration(value);
    }

    documentation {Get JMS transport header Expiration from the message
        returns int: The header value}
    public function getExpiration () returns @tainted int|error {
        return self.message.getExpiration();
    }

    documentation {Clear JMS properties of the message
        returns error if any JMS provider level internal error occur}
    public function clearProperties() {
        self.message.clearProperties();
    }

    documentation {Clears body of the JMS message
        returns error if any JMS provider level internal error occur}
    public function clearBody() returns error? {
        return self.message.clearBody();
    }

    documentation {Sets Priority JMS transport header to the message
        P{{value}} The header value}
    public function setPriority (@sensitive int value) returns error? {
        return self.message.setPriority(value);
    }

    documentation {Get JMS transport header Priority from the message
        returns The header value}
    public function getPriority () returns @tainted int|error {
        return self.message.getPriority();
    }

    documentation {Get JMS transport header Redelivered from the message
        returns The header value}
    public function getRedelivered () returns @tainted boolean|error {
        return self.message.getRedelivered();
    }

    documentation {Sets CorrelationID JMS transport header to the message
        P{{value}} The header value}
    public function setCorrelationID (@sensitive string value) returns error? {
        return self.message.setCorrelationID(value);
    }

    documentation {Get JMS transport header CorrelationID from the message
        returns The header value}
    public function getCorrelationID () returns @tainted string|error|() {
        return self.message.getCorrelationID();
    }
};
