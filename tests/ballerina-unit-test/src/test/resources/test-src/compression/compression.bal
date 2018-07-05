import ballerina/internal;

function decompressFile(string src, string destDir) returns error? {
    internal:Path srcPath = new(src);
    internal:Path dstPath = new(destDir);
    var result = internal:decompress(srcPath, dstPath);
    match result {
        internal:CompressionError err => return err;
        ()=> return;
    }
}

function compressFile(string src, string destDir) returns error? {
    internal:Path srcPath = new(src);
    internal:Path dstPath = new(destDir);
    var result =internal:compress(srcPath, dstPath);
    match result {
        internal:CompressionError err => return err;
        ()=> return;
    }
}

function decompressBlob(byte[] content, string destDir) returns error? {
    internal:Path dstPath = new(destDir);
    var result = internal:decompressFromByteArray(content, dstPath);
    match result {
        internal:CompressionError err => return err;
        ()=> return;
    }
}

function compressDirToBlob(string src) returns byte[]|error {
    internal:Path srcPath = new(src);
    var result = internal:compressToByteArray(srcPath);
    match result {
        internal:CompressionError err => return err;
        byte[] b => return b;
    }
}
