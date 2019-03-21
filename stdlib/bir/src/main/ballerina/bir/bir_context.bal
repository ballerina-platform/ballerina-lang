import ballerina/io;

# TODO Docs
public type BIRContext object {

    public function lookupBIRModule(ModuleID modId) returns Package {
        var modBinary = getBIRModuleBinary(self, modId);
        return populateBIRModuleFromBinary(modBinary);
    }
};

function getBIRModuleBinary(BIRContext birContext, ModuleID modId) returns byte[] = external;


// TODO Refactor following methods
function populateBIRModuleFromBinary(byte[] modBinary) returns Package {
    io:ReadableByteChannel byteChannel = io:createReadableChannel(modBinary);
    ChannelReader reader = new(byteChannel);
    checkValidBirChannel(reader);
    ConstPoolParser cpParser = new(reader);
    BirChannelReader birReader = new(reader, cpParser.parse());
    TypeParser typeParser = new (birReader);
    PackageParser pkgParser = new(birReader, typeParser);
    Package mod = pkgParser.parsePackage();
    return mod;
}

function checkValidBirChannel(ChannelReader reader) {
    checkMagic(reader);
    checkVersion(reader);
}

function checkMagic(ChannelReader reader) {
    byte[] baloCodeHexSpeak = [0xba, 0x10, 0xc0, 0xde];
    var magic = reader.readByteArray(4);

    if (!arrayEq(baloCodeHexSpeak, magic)){
        error err = error( "Invalid BIR binary content, unexptected header" );
        panic err;
    }
}

function checkVersion(ChannelReader reader) {
    var birVersion = reader.readInt32();
    var supportedBirVersion = 1;
    if (birVersion != 1){
        error err = error( "Unsupported BIR version " + birVersion + ", supports version " + supportedBirVersion);
        panic err;
    }
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

