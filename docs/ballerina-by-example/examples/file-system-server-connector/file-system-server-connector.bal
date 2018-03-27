import ballerina/log;
import ballerina/net.fs;

@Description {value:"In this particular scenario, the connector is listening to the directory specified by dirURI. "}
@Description {value:"File events that service get registered. "}
@Description {value:"Recursively monitor child directories or not. "}
endpoint fs:ServiceEndpoint inFolder {
    dirURI:"/home/ballerina/fs-server-connector/file-in",
    events:"create,delete,modify",
    recursive:false
};

@fs:serviceConfig {
    endpoints:[localFolder]
}
service<fs:Service> localObserver bind inFolder {
    sayHello (fs:FileSystemEvent m) {
        // Modified file name
        log:printInfo(m.name);
        // Service triggered event
        log:printInfo(m.operation);
    }
}