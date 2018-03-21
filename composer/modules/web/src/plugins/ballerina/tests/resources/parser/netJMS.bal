import ballerina/net.jms;

function testAcknowledge(message msg, string s){
    jms:acknowledge(msg, s);
}

function testCommit(message msg){
    jms:commit(msg);
}

function testRollback(message msg){
    jms:rollback(msg);
}