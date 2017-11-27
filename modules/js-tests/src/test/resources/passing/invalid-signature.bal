import ballerina.net.fs;
@fs:configuration {
    dirURI:"target/fs",
    events:"create,delete,modify",
    recursive:false
}
service<fs> fileSystem {
    resource fileResource (fs:SystemEvent m) {
        println(m.name);
        println(m.operation);
    }
}
