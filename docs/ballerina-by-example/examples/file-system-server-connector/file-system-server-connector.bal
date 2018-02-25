import ballerina.io;
import ballerina.net.fs;
import ballerina.lang.messages;

@Description {value:"In this particular scenario, the connector is listening to the directory specified by dirURI. "}
@Description {value:"The connector will be notified of any addition of files to the directory. "}
@Description {value:"Once the file is processed, it will be moved to another directory, specified by 'moveAfterProcess'."}
@fs:configuration {
    dirURI:"/home/ballerina/fs-server-connector/observed-dir",
    pollingInterval:"5000",
    actionAfterProcess:"MOVE",
    moveAfterProcess:"/home/ballerina/fs-server-connector/moveDir/",
    createMoveDir:"true"
}
service<fs> fileSystem {
    @Description {value:"A service bound to the file system can only contain one resource"}
    resource fileResource (message m) {
        // The message 'm' contains the URL of a file as its payload.
        // This URL can be then used in conjunction with another connector.
        io:println(messages:getStringPayload(m));
        reply m;
    }
}
