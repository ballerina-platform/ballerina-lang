import ballerina/websub;

service websubSubscriber on new websub:Listener(9092) {

}
