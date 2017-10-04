import ballerina.net.jms;

function testAcknowledge(jms:JMSMessage msg, string s){
    jms:acknowledge(msg, s);
}

function testCommit(jms:JMSMessage msg){
    jms:commit(msg);
}

function testRollback(jms:JMSMessage msg){
    jms:rollback(msg);
}
