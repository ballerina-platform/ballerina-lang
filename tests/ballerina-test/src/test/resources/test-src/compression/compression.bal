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

function decompressBlob(blob content, string destDir) returns error? {
    internal:Path dstPath = new(destDir);
    var result = internal:decompressFromBlob(content, dstPath);
    match result {
        internal:CompressionError err => return err;
        ()=> return;
    }
}

function compressDirToBlob(string src) returns blob|error {
    internal:Path srcPath = new(src);
    var result = internal:compressToBlob(srcPath);
    match result {
        internal:CompressionError err => return err;
        blob b => return b;
    }
}
