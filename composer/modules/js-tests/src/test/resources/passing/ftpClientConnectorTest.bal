import ballerina.net.ftp;
import ballerina.lang.files;

function testCopy(string sourcePath, string destPath) {
    ftp:ClientConnector ftpConnector = create ftp:ClientConnector();
    files:File source = {path : sourcePath};
    files:File dest = {path : destPath};
    ftpConnector.copyFile(source, dest);
}

function testMove(string sourcePath, string destPath) {
    ftp:ClientConnector ftpConnector = create ftp:ClientConnector();
    files:File source = {path : sourcePath};
    files:File dest = {path : destPath};
    ftpConnector.moveFile(source, dest);
}

function testDelete(string targetPath) {
    ftp:ClientConnector ftpConnector = create ftp:ClientConnector();
    files:File target = {path : targetPath};
    ftpConnector.deleteFile(target);
}

function testWrite(blob content, string targetPath) {
    ftp:ClientConnector ftpConnector = create ftp:ClientConnector();
    files:File target = {path : targetPath};
    ftpConnector.writeFile(content, target);
}

function testRead(string sourcePath, blob data) {
    ftp:ClientConnector ftpConnector = create ftp:ClientConnector();
    files:File source = {path : sourcePath};
    ftpConnector.write(data, source);
    blob b = ftpConnector.readFile(source);
}

function testCreate(string targetPath) {
    ftp:ClientConnector ftpConnector = create ftp:ClientConnector();
    files:File target = {path : targetPath};
    ftpConnector.createFile(target, false);
}

function testIsExist(string targetPath) {
    ftp:ClientConnector ftpConnector = create ftp:ClientConnector();
    files:File target = {path : targetPath};
    ftpConnector.exists(target);
}