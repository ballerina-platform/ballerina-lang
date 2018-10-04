import ballerina/file;
import ballerina/log;

@Description {value: "In this example, the listener monitors the directory specified in `path`. As the recursive property is set to false, " }
@Description {value: "the listener does not monitor the child directories of the main directory that it listens to." }
endpoint file:Listener inFolder {
    path: "/home/ballerina/fs-server-connector/observed-dir",
    recursive: false
};

service localObserver bind inFolder {
    onCreate(file:FileEvent m) {
        string msg = "Create: " + m.name;
        log:printInfo(msg);
    }
    onDelete(file:FileEvent m) {
        string msg = "Delete: " + m.name;
        log:printInfo(msg);
    }
    onModify(file:FileEvent m) {
        string msg = "Modify: " + m.name;
        log:printInfo(msg);
    }
}
