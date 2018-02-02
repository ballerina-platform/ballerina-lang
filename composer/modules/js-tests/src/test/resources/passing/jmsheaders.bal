import ballerina.net.jms;

function funcSetJMSCorrelationIDHeader (jms:JMSMessage msg, string value) (jms:JMSMessage) {
    msg.setCorrelationID(value);
    return msg;
}
function funcSetJMSDeliveryModeHeader (jms:JMSMessage msg, int value) (jms:JMSMessage) {
    msg.setDeliveryMode(value);
    return msg;
}
function funcSetJMSPriorityHeader (jms:JMSMessage msg, int value) (jms:JMSMessage) {
    msg.setPriority(value);
    return msg;
}
function funcSetJMSTypeHeader (jms:JMSMessage msg, string value) (jms:JMSMessage) {
    msg.setType(value);
    return msg;
}
function funcSetJMSExpirationHeader (jms:JMSMessage msg, int value) (jms:JMSMessage) {
    msg.setExpiration(value);
    return msg;
}

function funcGetJMSDeliveryModeHeader (jms:JMSMessage msg) (int) {
    return msg.getDeliveryMode();
}

function funcGetJMSExpirationHeader (jms:JMSMessage msg) (int) {
    return msg.getExpiration();
}

function funcGetJMSPriorityHeader (jms:JMSMessage msg) (int) {
    return msg.getPriority();
}

function funcGetJMSCorrelationIDHeader (jms:JMSMessage msg) (string) {
    return msg.getCorrelationID();
}

function funcGetJMSTypeHeader (jms:JMSMessage msg) (string) {
    return msg.getType();
}

function funcGetJMSMessageIDHeader (jms:JMSMessage msg) (string) {
    return msg.getMessageID();
}

function funcGetJMSTimestampHeader (jms:JMSMessage msg) (int) {
    return msg.getTimestamp();
}

function funcGetJMSRedeliveredHeader (jms:JMSMessage msg) (boolean) {
    return msg.getRedelivered();
}

