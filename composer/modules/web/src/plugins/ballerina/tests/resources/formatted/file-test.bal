import ballerina/lang.files;

function testCopy (string sourcePath, string destPath) {
    files:File source = {path:sourcePath};
    files:File dest = {path:destPath};
    files:copy(source, dest);
}

function testMove (string sourcePath, string destPath) {
    files:File source = {path:sourcePath};
    files:File dest = {path:destPath};
    files:move(source, dest);
}

function testExists (string targetPath) (boolean) {
    files:File target = {path:targetPath};
    boolean b = files:exists(target);
    return b;
}

function testDelete (string targetPath) {
    files:File target = {path:targetPath};
    files:delete(target);
}

function testOpen (string sourcePath, string accessMode) (files:File) {
    files:File source = {path:sourcePath};
    files:open(source, accessMode);
    return source;
}

function testWrite (blob content, string targetPath) {
    files:File target = {path:targetPath};
    files:open(target, "w");
    files:write(content, target);
}

function testWriteNewLine (blob content, string targetPath) {
    files:File target = {path:targetPath};
    files:open(target, "w");
    files:writeln(content, target);
}

function testWriteWithoutOpening (blob content, string targetPath) {
    files:File target = {path:targetPath};
    files:write(content, target);
}

function testRead (string sourcePath, int bytes) (blob, int) {
    files:File source = {path:sourcePath};
    files:open(source, "r");
    return files:read(source, bytes);
}

function testReadWithoutOpening (string sourcePath, int bytes) (blob, int) {
    files:File source = {path:sourcePath};
    return files:read(source, bytes);
}

function testClose (string sourcePath) (files:File) {
    files:File source = {path:sourcePath};
    files:open(source, "rw");
    files:close(source);
    return source;
}
