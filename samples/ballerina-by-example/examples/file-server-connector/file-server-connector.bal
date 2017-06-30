import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.net.file;

@doc:Description{value : "Service level annotation to define connection properties"}
@file:fileSource (
fileURI = "ftp://username:password@localhost:2221/orders",
pollingInterval = "1000"
)
service orderProcessService {
    @doc:Description{value : "Resource to process the file content"}
    resource processOrder(message m) {
        //Print the content of the file as a string value
        system:println(messages:getStringPayload(m));
        //Acknowledge that the file content processing is completed
        file:acknowledge(m);
    }
}
