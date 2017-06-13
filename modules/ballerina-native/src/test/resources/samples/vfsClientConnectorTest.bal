import ballerina.net.vfs;

connector vfsConnector = create vfs:ClientConnector();

function testCopy(string sourcePath, string destPath) (boolean) {
    files:File source = {path : sourcePath};
    files:File dest = {path : destPath};
    vfs:ClientConnector.create(vfsConnector, source, "file");
    vfs:ClientConnector.copy(vfsConnector, source, dest);
    boolean isExist = vfs:ClientConnector.isExist(vfsConnector, destPath);
    return isExist;
}

function testCopyNonExistent(string sourcePath, string destPath) (boolean) {
    files:File source = {path : sourcePath};
    files:File dest = {path : destPath};
    vfs:ClientConnector.copy(vfsConnector, source, dest);
}

function testCopyDir(string sourcePath, string destPath, string sourceDir, string destDir) (boolean, boolean) {
    files:File source = {path : sourceDir};
    files:File sourceFile = {path : sourcePath};
    files:File dest = {path : destDir};
    files:File destinationFile = {path : destPath};
    vfs:ClientConnector.create(vfsConnector, sourceFile, "file");
    vfs:ClientConnector.copy(vfsConnector, source, dest);
    boolean sourceExist = vfs:ClientConnector.isExist(vfsConnector, sourceFile);
    boolean destExist = vfs:ClientConnector.isExist(vfsConnector, destinationFile);
    return sourceExist, destExist;
}

function testMove(string sourcePath, string destPath) (boolean) {
    files:File source = {path : sourcePath};
    files:File dest = {path : destPath};
    vfs:ClientConnector.create(vfsConnector, sourcePath, "file");
    vfs:ClientConnector.move(vfsConnector, source, dest);
    boolean isExist = vfs:ClientConnector.isExist(vfsConnector, dest);
    return isExist;
}

function testMoveNonExistent(string sourcePath, string destPath) {
    files:File source = {path : sourcePath};
    files:File dest = {path : destPath};
    vfs:ClientConnector.move(vfsConnector, source, dest);
}

function testMoveDir(string sourcePath, string destPath, string sourceDir, string destDir) (boolean, boolean) {
    files:File source = {path : sourceDir};
    files:File sourceFile = {path : sourcePath};
    files:File dest = {path : destDir};
    files:File destinationFile = {path : destPath};
    vfs:ClientConnector.create(vfsConnector, sourceFile, "file");
    vfs:ClientConnector.move(vfsConnector, source, dest);
    boolean sourceExist = vfs:ClientConnector.isExist(vfsConnector, sourceFile);
    boolean destExist = vfs:ClientConnector.isExist(vfsConnector, destinationFile);
    return sourceExist, destExist;
}

function testDelete(string targetPath) (boolean) {
    files:File target = {path : targetPath};
    vfs:ClientConnector.delete(vfsConnector, target);
    boolean targetExist = vfs:ClientConnector.isExist(vfsConnector, target);
}

function testDeleteNonExistent(string sourcePath) {
    files:File source = {path : sourcePath};
    vfs:ClientConnector.delete(vfsConnector, source, dest);
}

function testDeleteDir(string sourcePath, string sourceDir) (boolean) {
    files:File source = {path : sourcePath};
    files:File directory = {path : sourceDir};
    vfs:ClientConnector.create(vfsConnector, source, "file");
    vfs:ClientConnector.delete(vfsConnector, directory);
    boolean sourceExist = vfs:ClientConnector.isExist(vfsConnector, directory);
    return sourceExist;
}

function testWrite(blob content, string targetPath) (boolean, blob) {
    files:File target = {path : targetPath};
    vfs:ClientConnector.write(vfsConnector, content, target);
    boolean isExist = vfs:ClientConnector.isExist(vfsConnector, target);
    blob b;
    if (isExist){
        b = vfs:ClientConnector.read(vfsConnector, target, 1000);
    }
    return b, isExist;

}

function testRead(string sourcePath, blob data, int bytes) (blob) {
    files:File source = {path : sourcePath};
    vfs:ClientConnector.write(vfsConnector, data, source);
    blob b = vfs:ClientConnector.read(vfsConnector, source, bytes);
    return b;
}

function testCreate(string targetPath) (boolean) {
    files:File target = {path : targetPath};
    vfs:ClientConnector.create(vfsConnector, target, "file");
    boolean isExist = vfs:ClientConnector.isExist(vfsConnector, target);
    return isExist;
}

function testIsExist(string sourcePath) (boolean) {
    files:File source = {path : sourcePath};
    vfs:ClientConnector.create(vfsConnector, target, "file");
    boolean isExist = vfs:ClientConnector.isExist(vfsConnector, target);
    return isExist;
}