import ballerina/io;

function main(string... args) {
    string srcFilePath = untaint args[0];
    string destFilePath = untaint args[1];
    io:ByteChannel channel = getFileChannel(srcFilePath, io:READ);
    var magic = channel.read(4);
    var versionValue = readInt(channel);
    readCp(channel);

    genPackage(readPackage(channel),destFilePath);
}

function genObjectFile(byte[] birBinary, string destFilePath, boolean dumpLLVMIR) {
    io:ByteChannel channel = io:createMemoryChannel(birBinary);
    var magic = channel.read(4);
    var versionValue = readInt(channel);
    readCp(channel);
    genPackage(readPackage(channel), destFilePath);
}

