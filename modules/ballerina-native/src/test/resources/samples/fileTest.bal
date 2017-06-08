import ballerina.lang.files;

function testCopy(files:File source, files:File dest) {
    files:copy(source, dest);
}

function testMove(files:File source, files:File dest) {
    files:move(source, dest);
}

function testDelete(files:File target) {
    files:delete(target);
}

function testOpen(files:File source, string accessMode) {
    files:open(source, accessMode);
}

function testWrite(blob content, files:File source) {
    files:open(source, "w");
    files:write(content, source);
}

function testRead(files:File source, int bytes)(blob, int) {
    return files:read(source, bytes);
}

function testClose(files:File source) {
    files:close(source);
}