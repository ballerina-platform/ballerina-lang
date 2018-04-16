import ballerina/compression;
import ballerina/file;

function decompressFile(string src, string destDir) returns error|() {
    file:Path srcPath = new(src);
    file:Path dstPath = new(destDir);
    var result = compression:decompress(srcPath, dstPath);
    match result {
        compression:CompressionError err => return err;
        ()=> return;
    }
}

function compressFile(string src, string destDir) returns error|() {
    file:Path srcPath = new(src);
    file:Path dstPath = new(destDir);
    var result =compression:compress(srcPath, dstPath);
    match result {
        compression:CompressionError err => return err;
        ()=> return;
    }
}

function decompressBlob(blob content, string destDir) returns error|() {
    file:Path dstPath = new(destDir);
    var result = compression:decompressFromBlob(content, dstPath);
    match result {
        compression:CompressionError err => return err;
        ()=> return;
    }
}

function compressDirToBlob(string src) returns blob|error {
    file:Path srcPath = new(src);
    var result = compression:compressToBlob(srcPath);
    match result {
        compression:CompressionError err => return err;
        blob b => return b;
    }
}
