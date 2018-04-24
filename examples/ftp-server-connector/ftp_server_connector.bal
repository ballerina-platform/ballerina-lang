import ballerina/net.ftp;
import ballerina/log;

// Creating an endpoint that listens to a remote directory.
endpoint ftp:ServiceEndpoint ftpServer1 {
    dirURI: "ftp://127.0.0.1/observed-dir/",
    pollingInterval: "2000",
    actionAfterProcess: "MOVE",
    moveAfterProcess: "ftp://127.0.0.1/move-to/",
    parallel: "false",
    createMoveDir: "true"
};

@ftp:serviceConfig {
}
// Creating a service that gets called as and when changes take place in the endpoint defined above.
service<ftp:Service> monitorServer bind ftpServer1 {
    fileResource(ftp:FTPServerEvent m) {
        // Print the file name
        log:printInfo(m.name);
        // Print the file size
        log:printInfo(m.size);
        // Print the last modified time
        log:printInfo(m.lastModifiedTimeStamp);
    }
}
