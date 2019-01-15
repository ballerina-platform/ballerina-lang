import ballerina/internal;

function decompressFile(string src, string destDir) returns error? {
    internal:Path srcPath = new(src);
    internal:Path dstPath = new(destDir);
    return internal:decompress(srcPath, dstPath);
}

function compressFile(string src, string destDir) returns error? {
    internal:Path srcPath = new(src);
    internal:Path dstPath = new(destDir);
    return internal:compress(srcPath, dstPath);
}

function decompressBlob(byte[] content, string destDir) returns error? {
    internal:Path dstPath = new(destDir);
    return internal:decompressFromByteArray(content, dstPath);
}

function compressDirToBlob(string src) returns byte[]|error {
    internal:Path srcPath = new(src);
    return internal:compressToByteArray(srcPath);
}
