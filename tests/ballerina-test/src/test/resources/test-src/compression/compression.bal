import ballerina/compression;

function testUnzipFile(string dirPath, string destDir, string folderToExtract) {
    compression:unzipFile(dirPath, destDir, folderToExtract);
}

function testZipFile(string dirPath, string destDir) {
    compression:zipFile(dirPath, destDir);
}

function testUnzipBytes(blob content, string destDir, string folderToExtract) {
    compression:unzipBytes(content, destDir, folderToExtract);
}

function testZipToBytes(string dirPath) returns (blob) {
    return compression:zipToBytes(dirPath);
}
