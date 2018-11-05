import ballerina/internal;

function decompressFile(string src, string destDir) returns internal:CompressionError? {
    internal:Path srcPath = new(src);
    internal:Path dstPath = new(destDir);
    return internal:decompress(srcPath, dstPath);
}

function compressFile(string src, string destDir) returns internal:CompressionError? {
    internal:Path srcPath = new(src);
    internal:Path dstPath = new(destDir);
    return internal:compress(srcPath, dstPath);
}

function decompressBlob(byte[] content, string destDir) returns internal:CompressionError? {
    internal:Path dstPath = new(destDir);
    return internal:decompressFromByteArray(content, dstPath);
}

function compressDirToBlob(string src) returns byte[]|internal:CompressionError {
    internal:Path srcPath = new(src);
    var result = internal:compressToByteArray(srcPath);
    if (result is internal:CompressionError) {
        return result;
    } else if(result is byte[]) {
        return result;
    } else {
        error err = error("error occurred");
        panic err;
    }
}
