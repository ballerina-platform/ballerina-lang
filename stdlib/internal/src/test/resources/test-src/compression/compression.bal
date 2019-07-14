import ballerina/internal;

function decompressFile(string src, string destDir) returns error? {
    return internal:decompress(src, destDir);
}

function compressFile(string src, string destDir) returns error? {
    return internal:compress(src, destDir);
}

function decompressBlob(byte[] content, string destDir) returns error? {
    return internal:decompressFromByteArray(content, destDir);
}

function compressDirToBlob(string src) returns byte[]|error {
    return internal:compressToByteArray(src);
}
