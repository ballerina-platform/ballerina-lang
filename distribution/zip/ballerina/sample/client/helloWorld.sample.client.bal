package client;
import ballerina.io;

function main (string[] args) {

     endpoint<helloWorldStub> helloWorldSampleClient {
            create helloWorldStub("0.0.0.0", 9090);
        }


}


service<grpc:Listener> helloWorldMessageListener {

    onMessage (string message) {
        io:println("Responce received from server: " + message);
    }

    onError (grpc:ServerError err) {
        if (err != null) {
            io:println("Error reported from server: " + err.message);
        }
    }

    onComplete () {
        io:println("Server Complete Sending Responses.");
    }
}


