import ballerina/file;
import ballerina/log;

@Description {value:"In this particular scenario, the connector is listening to the directory specified by path. "}
@Description {value:"Recursively monitor child directories or not. "}
endpoint file:Listener inFolder {
    path:"/home/ballerina/fs-server-connector/observed-dir",
    recursive:false
};

service localObserver bind inFolder {
    onCreate (file:FileEvent m) {
        string msg = "Create: " + m.name;
        log:printInfo(msg);
    }
    onDelete (file:FileEvent m) {
        string msg = "Delete: " + m.name;
        log:printInfo(msg);
    }
    onModify (file:FileEvent m) {
        string msg = "Modify: " + m.name;
        log:printInfo(msg);
    }
}
