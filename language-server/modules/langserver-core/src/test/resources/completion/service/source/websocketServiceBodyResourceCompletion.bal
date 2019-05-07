import ballerina/http;

service wsService on new http:WebSocketListener(9091) {

}
