import ballerina.net.ftp;
import ballerina.lang.files;

function testCopy(string sourcePath, string destPath) {
    ftp:ClientConnector ftpConnector = create ftp:ClientConnector();
    files:File source = {path : sourcePath};
    files:File dest = {path : destPath};
    ftp:ClientConnector.copy(ftpConnector, source, dest);
}

function testMove(string sourcePath, string destPath) {
    ftp:ClientConnector ftpConnector = create ftp:ClientConnector();
    files:File source = {path : sourcePath};
    files:File dest = {path : destPath};
    ftp:ClientConnector.move(ftpConnector, source, dest);
}

function testDelete(string targetPath) {
    ftp:ClientConnector ftpConnector = create ftp:ClientConnector();
    files:File target = {path : targetPath};
    ftp:ClientConnector.delete(ftpConnector, target);
}

function testWrite(blob content, string targetPath) {
    ftp:ClientConnector ftpConnector = create ftp:ClientConnector();
    files:File target = {path : targetPath};
    ftp:ClientConnector.write(ftpConnector, content, target);
}

function testRead(string sourcePath, blob data) {
    ftp:ClientConnector ftpConnector = create ftp:ClientConnector();
    files:File source = {path : sourcePath};
    ftp:ClientConnector.write(ftpConnector, data, source);
    blob b = ftp:ClientConnector.read(ftpConnector, source);
}

function testCreate(string targetPath) {
    ftp:ClientConnector ftpConnector = create ftp:ClientConnector();
    files:File target = {path : targetPath};
    ftp:ClientConnector.createFile(ftpConnector, target, "file");
}

function testIsExist(string targetPath) {
    ftp:ClientConnector ftpConnector = create ftp:ClientConnector();
    files:File target = {path : targetPath};
    ftp:ClientConnector.exists(ftpConnector, target);
}