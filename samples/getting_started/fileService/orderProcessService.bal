import ballerina.lang.message;
import ballerina.lang.system;
import ballerina.net.file;

@Source (
protocol = "file",
fileURI = "ftp://username:password@localhost:2221/orders",
pollingInterval = "1000")
service orderProcessService {

    resource processOrder(message m) {
        system:println(message:getStringPayload(m));
        file:acknowledge(m);
    }
}