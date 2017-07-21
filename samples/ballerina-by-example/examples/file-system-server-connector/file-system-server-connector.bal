import ballerina.net.fs;
import ballerina.lang.system;
import ballerina.lang.messages;
import ballerina.doc;

@fs:config {
    dirURI:"/home/ballerina/fs-server-connector/observed-dir",
    pollingInterval:"5000",
    fileSortAttribute:"name",
    fileSortAscending:"true",
    actionAfterProcess:"MOVE",
    moveAfterProcess:"/home/ballerina/fs-server-connector/moveDir/",
    createMoveDir:"true"
}
service<fs> fileSystem {
    @doc:Description {value:"A service bound to the file system can only contain one resource"}
    resource fileResource (message m) {
        // The message 'm' contains the URL of a file as its payload.
        // This URL can be then used in conjunction with another connector.
        system:println(messages:getStringPayload(m));
        reply m;
    }
}
