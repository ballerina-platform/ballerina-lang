import ballerina/io;
import ballerina/bir;
import ballerina/jvm;
import ballerina/reflect;

public type JarFile record {
    map<string> manifestEntries;
    map<byte[]> jarEntries;
};

public function main(string... args) {
    //do nothing
}

function genExecutableJar(bir:BIRContext birContext, bir:ModuleID entryModId, string destFilePath) returns JarFile {
    bir:Package entryMod = birContext.lookupBIRModule(entryModId);
    return generateJarFile(entryMod, destFilePath);
}

function generateJVMExecutable(byte[] birBinary, string progName) returns JarFile {
    io:ReadableByteChannel byteChannel = io:createReadableChannel(birBinary);
    bir:ChannelReader reader = new(byteChannel);
    checkValidBirChannel(reader);
    bir:ConstPoolParser cpParser = new(reader);
    bir:BirChannelReader birReader = new(reader, cpParser.parse());
    bir:TypeParser typeParser = new (birReader);
    bir:PackageParser pkgParser = new(birReader, typeParser);
    bir:Package pkg = pkgParser.parsePackage();
    return generateJarFile(pkg, progName);
}

function generateJarFile(bir:Package pkg, string progName) returns JarFile {
    //todo : need to generate java package here based on BIR package(s)
    invokedClassName = progName;

    // TODO: remove once the package init class is introduced
    typeOwnerClass = progName;

    jvm:ClassWriter cw = new(COMPUTE_FRAMES);
    cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, invokedClassName, null, OBJECT_VALUE, null);

    map<byte[]> jarEntries = {};
    map<string> manifestEntries = {};

    generateUserDefinedTypeFields(cw, pkg.typeDefs);

    bir:Function? mainFunc = getMainFunc(pkg.functions);
    if (mainFunc is bir:Function) {
        generateMainMethod(mainFunc, cw, pkg);
        manifestEntries["Main-Class"] = invokedClassName;
    }

    foreach var func in pkg.functions {
        generateMethod(func, cw);
    }

    cw.visitEnd();

    byte[] classContent = cw.toByteArray();

    jarEntries[invokedClassName + ".class"] = classContent;
    JarFile jarFile = {jarEntries : jarEntries, manifestEntries : manifestEntries};
    return jarFile;
}

function getMainFunc(bir:Function[] funcs) returns bir:Function? {
    bir:Function? userMainFunc = ();
    foreach var func in funcs {
        if (func.name.value == "main") {
            userMainFunc = untaint func;
            break;
        }
    }

    return userMainFunc;
}

function checkValidBirChannel(bir:ChannelReader reader) {
    checkMagic(reader);
    checkVersion(reader);
}

function checkMagic(bir:ChannelReader reader) {
    byte[] baloCodeHexSpeak = [0xba, 0x10, 0xc0, 0xde];
    var magic = reader.readByteArray(4);

    if (!arrayEq(baloCodeHexSpeak, magic)){
        error err = error( "Invalid BIR binary content, unexptected header" );
        panic err;
    }
}

function checkVersion(bir:ChannelReader reader) {
    var birVersion = reader.readInt32();
    var supportedBirVersion = 1;
    if (birVersion != 1){
        error err = error( "Unsupported BIR version " + birVersion + ", supports version " + supportedBirVersion);
        panic err;
    }
}

function openReadableFile(string filePath) returns io:ReadableByteChannel {
    io:ReadableByteChannel byteChannel = io:openReadableFile(filePath);
    return byteChannel;
}

function arrayEq(byte[] x, byte[] y) returns boolean {
    var xLen = x.length();

    if xLen != y.length() {
        return false;
    }

    int i = 0;
    while i < xLen {
        if (x[i] != y[i]){
            return false;
        }
        i = i + 1;
    }
    return true;
}
