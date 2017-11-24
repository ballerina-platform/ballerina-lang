import ballerina.net.fs;
@fs:configuration {
    dirURI:"target/fs",
    events:"create,delete,modify",
    recursive:false
}
service<fs> fileSystem {
    resource fileResource1 (fs:FileSystemEvent m) {
        println(m.name);
        println(m.operation);
    }
    resource fileResource2 (fs:FileSystemEvent m) {
        println(m.name);
        println(m.operation);
    }
    resource fileResource3 (fs:FileSystemEvent m) {
        println(m.name);
        println(m.operation);
    }
}
