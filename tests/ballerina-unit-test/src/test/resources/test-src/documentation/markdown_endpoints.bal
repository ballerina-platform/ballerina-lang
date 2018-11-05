import ballerina/file;

# Documentation for the endpoint.
endpoint file:Listener localFolder {
    path: "target/fs"
};

# Documentation for the public endpoint.
public endpoint file:Listener localFolder2 {
    path: "target/fs"
};

public function test(string s) {

}
