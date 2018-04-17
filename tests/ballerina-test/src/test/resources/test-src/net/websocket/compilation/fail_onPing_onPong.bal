import ballerina/http;
import ballerina/io;

endpoint http:WebSocketListener echoEP {
    host:"0.0.0.0",
    port:9090
};

@http:WebSocketServiceConfig {
    path:"/echo"
}
service<http:WebSocketService> echo bind echoEP {

    onPing(endpoint conn, boolean so){

    }
    onPong(endpoint conn){

    }
}