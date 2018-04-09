package ballerina.jms;

public type Message object {
    documentation {Gets text content of the JMS message
        returns message content as string}
    public native function getTextMessageContent () returns (string);

    documentation {Sets a JMS transport string property from the message
        P{{key}} The string property name
        P{{value}} The string property value}
    public native function setStringProperty (string key, string value) returns Error|();

    documentation {Gets a JMS transport string property from the message
        P{{key}} The string property name
        returns The string property value}
    public native function getStringProperty (string key) returns string|()|Error;

    documentation {Sets a JMS transport integer property from the message
        P{{key}} The integer property name
        P{{value}} The integer property value}
    public native function setIntProperty (string key, int value) returns Error|();

    documentation {Gets a JMS transport integer property from the message
        P{{key}} The integer property name
        returns The integer property value}
    public native function getIntProperty (string key) returns int|Error;

    documentation {Sets a JMS transport boolean property from the message
        P{{key}} The boolean property name
        P{{value}} The boolean property value}
    public native function setBooleanProperty (string key, boolean value) returns Error|();

    documentation {Gets a JMS transport boolean property from the message
        P{{key}} The boolean property name
        returns The boolean property value}
    public native function getBooleanProperty (string key) returns boolean|Error;

    documentation {Sets a JMS transport float property from the message
        P{{key}} The float property name
        P{{value}} The float property value}
    public native function setFloatProperty (string key, float value) returns Error|();

    documentation {Gets a JMS transport float property from the message
        P{{key}} The float property name
        returns The float property value}
    public native function getFloatProperty (string key) returns float|Error;


    documentation {Clear JMS properties of the message
        returns Error if any JMS provider level internal error occur}
    public native function clearProperties();

    documentation {Clears body of the JMS message
        returns Error if any JMS provider level internal error occur}
    public native function clearBody() returns Error|() ;
};


