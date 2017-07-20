import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.net.fs;

@Source (
dirPath = "ftp://username:password@localhost:2221/orders",
pollingInterval = "1000"
)
service<fs> orderProcessService {

    resource processOrder(message m) {
        system:println(messages:getStringPayload(m));
        reply;
    }
}
