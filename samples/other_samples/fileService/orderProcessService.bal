import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.net.file;

@Source (
protocol = "file",
fileURI = "file:///home/user/orders",
pollingInterval = "10000"
)
service orderProcessService {

    resource processOrder(message m) {
        system:println(messages:getStringPayload(m));
        file:acknowledge(m);
    }
}
