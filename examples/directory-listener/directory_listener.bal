import ballerina/file;
import ballerina/log;

// In this example, the listener monitors any modifications done to a specific directoy.
// Before running the example, change the value of the 'path' field 
// to indicate the path of the directory that you want the listener to monitor.
// As the recursive property is set to false,
// the listener does not monitor the child directories of the main directory
// that it listens to.
listener file:Listener inFolder = new ({
    path: "/home/ballerina/fs-server-connector/observed-dir",
    recursive: false
});

// The directory listener should have at least one of these predefined resources.
service localObserver on inFolder {

    // This resource is invoked once a new file is created in the listening directory.
    resource function onCreate(file:FileEvent m) {
        string msg = "Create: " + m.name;
        log:printInfo(msg);
    }

    // This resource is invoked once an existing file is deleted from the listening directory.
    resource function onDelete(file:FileEvent m) {
        string msg = "Delete: " + m.name;
        log:printInfo(msg);
    }

    // This resource is invoked once an existing file is modified in the listening directory.
    resource function onModify(file:FileEvent m) {
        string msg = "Modify: " + m.name;
        log:printInfo(msg);
    }
}
