import ballerina/jballerina.java;

// Creates a file in the current directory.
public function main(string filepath) returns error? {
   handle file = newFile(java:fromString(filepath));
   boolean _ = check createNewFileInternal(file);
}

public function createNewFileInternal(handle receiver) returns boolean | error = @java:Method {
    name: "createNewFile",
    'class: "java/io/File",
    paramTypes: []
} external;

function newFile(handle filename) returns handle = @java:Constructor {
   'class: "java.io.File",
   paramTypes: ["java.lang.String"]
} external;
