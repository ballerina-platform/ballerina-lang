import ballerina.net.fs;

service<fs> fileSystem {
    resource fileResource (fs:FileSystemEvent m) {
        println(m.name);
        println(m.operation);
    }
}
