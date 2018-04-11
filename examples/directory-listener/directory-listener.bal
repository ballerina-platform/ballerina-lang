import ballerina/file;
import ballerina/log;

@Description {value:"In this particular scenario, the listener is listening to the directory specified by path and "}
@Description {value:"will not trigger any events that happen to child directories as the recursive property set to false. "}
endpoint file:Listener inFolder {
    path:"/Users/shankar/temp/fs-server-connector/observed-dir",
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
