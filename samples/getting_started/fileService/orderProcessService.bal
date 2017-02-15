import ballerina.lang.message;
import ballerina.lang.system;
import ballerina.net.file;

@Source (
protocol = "file",
fileURI = "file:///home/user/orders",
pollingInterval = "1000")
service orderProcessService {

    @file:OnMessage
    resource processOrder(message m) {
        system:println(message:getStringPayload(m));
        file:acknowledge(m);
    }
}