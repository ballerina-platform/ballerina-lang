import ballerina/file;
import ballerina/log;

// In this example, the listener monitors the directory specified in `path`.
// As the recursive property is set to false,
// the listener does not monitor the child directories of the main directory
// that it listens to.
listener file:Listener inFolder = new ({
    path: "/home/ballerina/fs-server-connector/observed-dir",
    recursive: false
});


service localObserver on inFolder {
    resource function onCreate(file:FileEvent m) {
        string msg = "Create: " + m.name;
        log:printInfo(msg);
    }
    resource function onDelete(file:FileEvent m) {
        string msg = "Delete: " + m.name;
        log:printInfo(msg);
    }
    resource function onModify(file:FileEvent m) {
        string msg = "Modify: " + m.name;
        log:printInfo(msg);
    }
}
