import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.net.file;

@Source (
protocol = "file",
fileURI = "ftp://username:password@localhost:2221/orders",
pollingInterval = "1000"
)
service orderProcessService {

    resource processOrder(message m) {
        system:println(messages:getStringPayload(m));
        file:acknowledge(m);
    }
}
