import ballerina.net.jms;

function funcSetStringProperty (jms:JMSMessage msg, string name, string value) (jms:JMSMessage) {
    msg.setStringProperty(name, value);
    return msg;
}
function funcSetIntProperty (jms:JMSMessage msg, string name, int value) (jms:JMSMessage) {
    msg.setIntProperty(name, value);
    return msg;
}
function funcSetBooleanProperty (jms:JMSMessage msg, string name, boolean value) (jms:JMSMessage) {
    msg.setBooleanProperty(name, value);
    return msg;
}
function funcSetFloatProperty (jms:JMSMessage msg, string name, float value) (jms:JMSMessage) {
    msg.setFloatProperty(name, value);
    return msg;
}

function funcGetStringProperty (jms:JMSMessage msg, string name) (string) {
    return msg.getStringProperty(name);
}
function funcGetIntProperty (jms:JMSMessage msg, string name) (int) {
    return msg.getIntProperty(name);
}
function funcGetBooleanProperty (jms:JMSMessage msg, string name) (boolean) {
    return msg.getBooleanProperty(name);
}
function funcGetFloatProperty (jms:JMSMessage msg, string name) (float) {
    return msg.getFloatProperty(name);
}
