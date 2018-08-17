import ballerina/io;
import ballerina/internal;

string[] cpStrings;
PackageId[] cpPackages;
int[] cpInts;

function getFileChannel(string filePath,
                        io:Mode permission) returns io:ByteChannel {
    io:ByteChannel channel = io:openFile(filePath, permission);

    return channel;
}
function readBytes(io:ByteChannel channel,
                   int numberOfBytes) returns (byte[], int) {
    var result = channel.read(numberOfBytes);
    match result {
        (byte[], int) content => {
            return content;
        }
        error readError => {
            throw readError;
        }
    }
}
function writeBytes(io:ByteChannel channel,
                    byte[] content,
                    int startOffset = 0) returns int {
    var result = channel.write(content, startOffset);
    match result {
        int numberOfBytesWritten => {
            return numberOfBytesWritten;
        }
        error err => {
            throw err;
        }
    }
}
function copy(io:ByteChannel src, io:ByteChannel dst) {
    int bytesChunk = 10000;
    int numberOfBytesWritten = 0;
    int readCount = 0;
    int offset = 0;
    byte[] readContent;
    boolean doneCopying = false;
    try {
        while (!doneCopying) {
            (readContent, readCount) = readBytes(src, 1000);
            if (readCount <= 0) {
                doneCopying = true;
            }
            numberOfBytesWritten = writeBytes(dst, readContent);
        }
    } catch (error err) {
        throw err;
    }
}

type PackageId record {
    string name;
    string versionValue;
    string org;
};

function readCp(io:ByteChannel channel) {
    var cpCount = readInt(channel);
    int i = 0;
    while (i < cpCount) {
        var cpType = readByte(channel);
        if (cpType == 1){
            cpInts[i] = readLong(channel);
        } else if (cpType == 4){
            cpStrings[i] = readString(channel);
        } else if (cpType == 5){
            PackageId id = { org: cpStrings[readInt(channel)],
                name: <string>cpStrings[readInt(channel)],
                varstionVallue: <string>cpStrings[readInt(channel)] };
            cpPackages[i] = id;
        } else {
            error err = { message: "cp type " + cpType + " not supported.:" };
            throw err;
        }
        i++;
    }
}

function readPackage(io:ByteChannel channel) returns BIRPackage {
    var pkgIdCp = readInt(channel);
    var numFuncs = readInt(channel);
    BIRFunction[] funcs;
    int i;
    while (i < numFuncs) {
        funcs[i] = readFunction(channel);
        i++;
    }
    return { functions:funcs };
}

function readVarKind(io:ByteChannel channel) returns VarKind {
    var (enumByte, _mustBe1) = check channel.read(1);
    int b = <int>enumByte[0];
    if (b == 1){
        return "LOCAL";
    } else if (b == 2){
        return "ARG";
    } else if (b == 3){
        return "TEMP";
    } else if (b == 4){
        return "RETURN";
    }
    error unknownVisibility;
    throw unknownVisibility;
}

function readVariableDcl(io:ByteChannel channel) returns BIRVariableDcl {
    var kind = readVarKind(channel);
    BIRVariableDcl dcl = {
        typeValue: readBType(channel),
        name: { value: readStringViaCp(channel) },
        kind:kind
    };
    return dcl;
}

function readFunction(io:ByteChannel channel) returns BIRFunction {
    var name = readStringViaCp(channel);
    var isDeclaration = readBoolean(channel);
    var visibility = readVisibility(channel);
    //TODO assign to func
    var sigCp = readInt(channel);
    var argsCount = readInt(channel);
    var numLocalVars = readInt(channel);

    BIRVariableDcl[] dcls;
    map<BIRVariableDcl> localVarMap;
    int i;
    while (i < numLocalVars) {
        var dcl = readVariableDcl(channel);
        dcls[i] = dcl;
        localVarMap[dcl.name.value] = dcl;
        i++;
    }

    BIRBasicBlock[] basicBlocks;
    var numBB = readInt(channel);
    i = 0;
    while (i < numBB) {
        basicBlocks[i] = readBB(channel, localVarMap);
        i++;
    }

    return {
        name: { value: name },
        isDeclaration: isDeclaration,
        visibility: visibility,
        localVars: dcls,
        basicBlocks: basicBlocks,
        argsCount: argsCount
        //typeValue:
    };
}
function readBB(io:ByteChannel channel, map<BIRVariableDcl> localVarMap) returns BIRBasicBlock {
    var id = readStringViaCp(channel);
    var numInstruction = readInt(channel) - 1;
    BIRInstruction[] instructions;
    int i;
    while (i < numInstruction) {
        instructions[i] = readInstruction(channel, localVarMap);
        i++;
    }
    return { id: { value: id },
        instructions: instructions,
        terminator: readTerminator(channel, localVarMap) };
}

function readTerminator(io:ByteChannel channel, map<BIRVariableDcl> localVarMap) returns BIRTerminator {
    var kindTag = readByte(channel);
    if (kindTag == 3){
        InstructionKind kind = "BRANCH";
        var op = readVarRef(channel, localVarMap);
        BIRBasicBlock trueBB = readBBRef(channel);
        BIRBasicBlock falseBB = readBBRef(channel);
        return new Branch(falseBB, kind, op, trueBB);
    } else if (kindTag == 1){
        InstructionKind kind = "GOTO";
        return new GOTO(kind, readBBRef(channel));
    } else if (kindTag == 4){
        InstructionKind kind = "RETURN";
        return new Return(kind);
    }
    error err = { message: "instrucion kind " + kindTag + " not impl." };
    throw err;
}

function readBBRef(io:ByteChannel channel) returns BIRBasicBlock {
    return { id: { value: readStringViaCp(channel) } };
}

function readInstruction(io:ByteChannel channel, map<BIRVariableDcl> localVarMap) returns BIRInstruction {
    var kindTag = readByte(channel);
    if (kindTag == 6){
        //TODO: remove redundent
        var bType = readBType(channel);
        InstructionKind kind = "CONST_LOAD";
        var constLoad = new ConstantLoad(kind,
            readVarRef(channel, localVarMap),
            bType,
            readIntViaCp(channel));
        return constLoad;
    } else if (kindTag == 5){
        InstructionKind kind = "MOVE";
        var rhsOp = readVarRef(channel, localVarMap);
        var lhsOp = readVarRef(channel, localVarMap);
        return new Move(kind, lhsOp, rhsOp);
    } else if (kindTag == 16){
        InstructionKind kind = "LESS_THAN";
        var rhsOp1 = readVarRef(channel, localVarMap);
        var rhsOp2 = readVarRef(channel, localVarMap);
        var lhsOp = readVarRef(channel, localVarMap);
        return new BinaryOp (kind, lhsOp, rhsOp1, rhsOp2,
            //TODO: remove type, not used
            "int");
    } else if (kindTag == 7){
        InstructionKind kind = "ADD";
        var rhsOp1 = readVarRef(channel, localVarMap);
        var rhsOp2 = readVarRef(channel, localVarMap);
        var lhsOp = readVarRef(channel, localVarMap);
        return new BinaryOp (kind, lhsOp, rhsOp1, rhsOp2,
            //TODO: remove type, not used
            "int");
    }
    error err = { message: "instrucion kind " + kindTag + " not impl." };
    throw err;
}

function readVarRef(io:ByteChannel channel, map<BIRVariableDcl> localVarMap) returns BIRVarRef {
    var varName = readStringViaCp(channel);
    var decl = getDecl(localVarMap, varName);
    return new BIRVarRef("VAR_REF", decl.typeValue, decl);
}

function getDecl(map<BIRVariableDcl> localVarMap, string varName) returns BIRVariableDcl {
    var posibalDcl = localVarMap[varName];
    match posibalDcl {
        () => {
            error err = {};
            throw err;
        }
        BIRVariableDcl dcl => return dcl;
    }
}

function readLong(io:ByteChannel channel) returns int {
    return readInt(channel) << 32|readInt(channel);
}

function readInt(io:ByteChannel channel) returns int {
    var (intBytes, _mustBe4) = check channel.read(4);
    return bytesToInt(intBytes);
}

function readBoolean(io:ByteChannel channel) returns boolean {
    var (boolByte, _mustBe1) = check channel.read(1);
    byte one = 1;
    return boolByte[0] == one;
}

function readByte(io:ByteChannel channel) returns int {
    var (byteValue, _mustBe1) = check channel.read(1);
    return <int>byteValue[0];
}
function readBType(io:ByteChannel channel) returns BType {
    string sginatureAlias = readStringViaCp(channel);
    if (sginatureAlias == "I"){
        return "int";
    } else if (sginatureAlias == "B"){
        return "boolean";
    }
    error err = { message: "type signature " + sginatureAlias + " not supported." };
    throw err;
}


function readVisibility(io:ByteChannel channel) returns Visibility {
    var (enumByte, _mustBe1) = check channel.read(1);
    int b = <int>enumByte[0];
    if (b == 0){
        return "PACKAGE_PRIVATE";
    } else if (b == 1){
        return "PRIVATE";
    } else if (b == 2){
        return "PUBLIC";
    }
    error unknownVisibility;
    throw unknownVisibility;
}

function bytesToInt(byte[] b) returns int {
    int ff = 255;
    int octave1 = 8;
    int octave2 = 16;
    int octave3 = 24;
    int b0 = <int>b[0];
    int b1 = <int>b[1];
    int b2 = <int>b[2];
    int b3 = <int>b[3];
    return b0 <<octave3|(b1 & ff) <<octave2|(b2 & ff) <<octave1|(b3 & ff);
}

function readStringViaCp(io:ByteChannel channel) returns string {
    return cpStrings[readInt(channel)];
}

function readIntViaCp(io:ByteChannel channel) returns int {
    return cpInts[readInt(channel)];
}

function readString(io:ByteChannel channel) returns string {
    var stringLen = readInt(channel);
    var (strBytes, strLen) = check channel.read(untaint stringLen);
    return internal:byteArrayToString(strBytes, "UTF-8");
}


