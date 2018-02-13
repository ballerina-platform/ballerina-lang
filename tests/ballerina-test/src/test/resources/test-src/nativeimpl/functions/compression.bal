import ballerina.compression;

function testUnzipFile(string dirPath, string destDir) {
    compression:unzipFile(dirPath, destDir);
}

function testZipFile(string dirPath, string destDir) {
    compression:zipFile(dirPath, destDir);
}

function testUnzipBytes(blob content, string destDir) {
    compression:unzipBytes(content, destDir);
}

function testZipToBytes(string dirPath) (blob) {
    return compression:zipToBytes(dirPath);
}