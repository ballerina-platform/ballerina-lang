import ballerina/http;
import ballerina/io;

endpoint http:Client clientEP {
    url:"http://localhost:9218",
    proxy: {
        host:"localhost",
        port:9219
    }
};

function main (string... args) {
    http:Request req = new;
    var resp = clientEP -> post("/proxy/server", req);
    match resp {
        error err => io:println(err.message);
        http:Response response => {
            match (response.getTextPayload()) {
                error payloadError => io:println(payloadError.message);
                string res => io:println(res);
            }
        }
    }
}
