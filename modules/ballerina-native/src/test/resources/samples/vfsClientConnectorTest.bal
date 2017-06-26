import ballerina.net.vfs;
import ballerina.lang.files;

function testCopy(string sourcePath, string destPath) {
    vfs:ClientConnector vfsConnector = create vfs:ClientConnector();
    files:File source = {path : sourcePath};
    files:File dest = {path : destPath};
    vfs:ClientConnector.copy(vfsConnector, source, dest);
}

function testMove(string sourcePath, string destPath) {
    vfs:ClientConnector vfsConnector = create vfs:ClientConnector();
    files:File source = {path : sourcePath};
    files:File dest = {path : destPath};
    vfs:ClientConnector.move(vfsConnector, source, dest);
}

function testDelete(string targetPath) {
    vfs:ClientConnector vfsConnector = create vfs:ClientConnector();
    files:File target = {path : targetPath};
    vfs:ClientConnector.delete(vfsConnector, target);
}

function testWrite(blob content, string targetPath) {
    vfs:ClientConnector vfsConnector = create vfs:ClientConnector();
    files:File target = {path : targetPath};
    vfs:ClientConnector.write(vfsConnector, content, target);
}

function testRead(string sourcePath, blob data, int bytes) {
    vfs:ClientConnector vfsConnector = create vfs:ClientConnector();
    files:File source = {path : sourcePath};
    vfs:ClientConnector.write(vfsConnector, data, source);
    blob b = vfs:ClientConnector.read(vfsConnector, source, bytes);
}

function testCreate(string targetPath) {
    vfs:ClientConnector vfsConnector = create vfs:ClientConnector();
    files:File target = {path : targetPath};
    vfs:ClientConnector.createFile(vfsConnector, target, "file");
}

function testIsExist(string targetPath) {
    vfs:ClientConnector vfsConnector = create vfs:ClientConnector();
    files:File target = {path : targetPath};
    vfs:ClientConnector.exists(vfsConnector, target);
}