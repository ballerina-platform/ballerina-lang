import ballerina.net.jms;

function testGetMessageType(message msg)(string){
    return jms:getMessageType(msg);
}

function testAcknowledge(message msg, string s){
    jms:acknowledge(msg, s);
}

function testCommit(message msg){
    jms:commit(msg);
}

function testRollback(message msg){
    jms:rollback(msg);
}