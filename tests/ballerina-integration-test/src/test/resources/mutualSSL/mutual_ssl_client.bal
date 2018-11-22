import ballerina/http;
import ballerina/io;
import ballerina/mime;

endpoint http:Client clientEP {
    url:"https://localhost:9116",
    secureSocket:{
        keyStore:{
            path:"${ballerina.home}/bre/security/ballerinaKeystore.p12",
            password:"ballerina"
        },
        trustStore:{
            path:"${ballerina.home}/bre/security/ballerinaTruststore.p12",
            password:"ballerina"
        },
        protocol:{
            name:"TLSv1.2",
            versions:["TLSv1.2", "TLSv1.1"]
        },
        ciphers:["TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA"],
        certValidation: {
            enable: false
        },
        ocspStapling: false
    }
};

public function main (string... args) {
    http:Request req = new;
    var resp = clientEP -> get("/echo/");

    if (resp is http:Response) {
        var payload = resp.getTextPayload();
        if (payload is string) {
            io:println(payload);
        } else if (payload is error) {
            io:println(<string> payload.detail().message);
        }
    } else if (resp is error) {
        io:println(<string> resp.detail().message);
    }
}
