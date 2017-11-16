import ballerina.net.fs;

function testFileMove(fs:FileSystemEvent event, string path) {
    event.move(path);
}

function testFileDelete(fs:FileSystemEvent event) {
    event.delete();
}