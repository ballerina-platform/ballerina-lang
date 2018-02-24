import ballerina.net.fs;

function testFileMove(fs:FileSystemEvent event, string path) {
    event.moveFile(path);
}

function testFileDelete(fs:FileSystemEvent event) {
    event.deleteFile();
}