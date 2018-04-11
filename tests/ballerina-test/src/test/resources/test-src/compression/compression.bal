import ballerina/compression;
import ballerina/file;

function decompressFile(string src, string destDir) returns (error) {
    file:Path srcPath = new(src);
    file:Path dstPath = new(destDir);
    compression:CompressionError err = compression:decompress(srcPath, dstPath);
    return err;
}

function compressFile(string src, string destDir) returns (error) {
    file:Path srcPath = new(src);
    file:Path dstPath = new(destDir);
    compression:CompressionError err =compression:compress(srcPath, dstPath);
    return err;
}

function decompressBlob(blob content, string destDir) returns (error) {
    file:Path dstPath = new(destDir);
    compression:CompressionError err = compression:decompressFromBlob(content, dstPath);
    return err;
}

function compressDirToBlob(string src) returns (blob|error) {
    file:Path srcPath = new(src);
    var result = compression:compressToBlob(srcPath);
    match result {
        compression:CompressionError err => return err;
        blob b => return b;
    }
}
