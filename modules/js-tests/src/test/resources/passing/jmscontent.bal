import ballerina.net.jms;

function funcSetJMSTextContent(jms:JMSMessage msg, string content) (jms:JMSMessage) {
    msg.setTextMessageContent(content);
    return msg;
}
function funcGetJMSTextContent(jms:JMSMessage msg) (string ) {
    return msg.getTextMessageContent();
}
function funcClearJMSTextContent(jms:JMSMessage msg) (string) {
    msg.clearBody();
    return msg.getTextMessageContent();
}
