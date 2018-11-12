import ballerina/io;
import ballerina/bir;

public function main(string... args) {
    var (srcFilePath, destFilePath) = parseArgs(args);
    genObjectFileFromChannel(openReadableFile(srcFilePath), destFilePath, true);
}

function genObjectFile(byte[] birBinary, string destFilePath, boolean dumpLLVMIR) {
    io:ReadableByteChannel byteChannel = io:createReadableChannel(birBinary);
    genObjectFileFromChannel(byteChannel, destFilePath, dumpLLVMIR);
}

function genObjectFileFromChannel(io:ReadableByteChannel byteChannel, string destFilePath, boolean dumpLLVMIR) {
    bir:ChannelReader reader = new(byteChannel);
    checkValidBirChannel(reader);
    bir:ConstPoolParser cpParser = new(reader);
    bir:BirChannelReader birReader = new(reader, cpParser.parse());
    bir:PackageParser p = new(birReader);
    genPackage(p.parsePackage(), destFilePath, dumpLLVMIR);
}

function parseArgs(string[] args) returns (string, string) {
    var argLen = args.length();
    if (argLen != 2){
        error err = error("Usage: compiler_backend_llvm <path-to-bir> <part-to-output-obj>");
        panic err;
    }
    return (untaint args[0], untaint args[1]);
}

function checkValidBirChannel(bir:ChannelReader reader) {
    checkMagic(reader);
    checkVersion(reader);
}

function checkMagic(bir:ChannelReader reader) {
    byte[] baloCodeHexSpeak = [0xba, 0x10, 0xc0, 0xde];
    var magic = reader.readByteArray(4);

    if (!arrayEq(baloCodeHexSpeak, magic)){
        error err = error("Invalid BIR binary content, unexptected header");
        panic err;
    }
}

function checkVersion(bir:ChannelReader reader) {
    var birVersion = reader.readInt32();
    var supportedBirVersion = 1;
    if (birVersion != 1){
        error err = error("Unsupported BIR version " + birVersion + ", supports version " + supportedBirVersion);
        panic err;
    }
}


function openReadableFile(string filePath) returns io:ReadableByteChannel {
    io:ReadableByteChannel byteChannel = io:openReadableFile(filePath);
    return byteChannel;
}

function arrayEq(byte[] x, byte[] y) returns boolean {
    var xLen = x.length();

    if xLen != y.length(){
        return false;
    }

    int i = 0;
    while i < xLen {
        if (x[i] != y[i]){
            return false;
        }
        i += 1;
    }
    return true;
}
