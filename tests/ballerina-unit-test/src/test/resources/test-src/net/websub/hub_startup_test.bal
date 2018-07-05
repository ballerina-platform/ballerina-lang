import ballerina/io;
import ballerina/websub;

function startupHub(int hubPort) returns websub:WebSubHub|websub:HubStartedUpError {
    return websub:startUpBallerinaHub(port = hubPort, sslEnabled = false);
}

function stopHub(websub:WebSubHub|websub:HubStartedUpError hubStartUpResult) returns boolean {
    websub:WebSubHub hub = hubStartUpResult but {
                                    websub:HubStartedUpError hubStartedUpErr => hubStartedUpErr.startedUpHub };
    return hub.stop();
}
