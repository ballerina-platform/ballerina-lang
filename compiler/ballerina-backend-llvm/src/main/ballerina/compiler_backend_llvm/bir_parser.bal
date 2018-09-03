import ballerina/io;
import ballerina/internal;

type PackageId record {
    string name;
    string versionValue;
    string org;
};

type ConstPool record {
    PackageId[] packages;
    string[] strings;
    int[] ints;
};

type BirParser object {
    io:ByteChannel channel,
    ConstPool cp;

    new(channel) {
    }

    function readCp() {
        var cpCount = readInt(channel);
        int i = 0;
        while (i < cpCount) {
            var cpType = readByte(channel);
            if (cpType == 1){
                cp.ints[i] = readLong(channel);
            } else if (cpType == 4){
                cp.strings[i] = readString(channel);
            } else if (cpType == 5){
                PackageId id = { org: cp.strings[readInt(channel)],
                    name: <string>cp.strings[readInt(channel)],
                    varstionVallue: <string>cp.strings[readInt(channel)] };
                cp.packages[i] = id;
            } else {
                error err = { message: "cp type " + cpType + " not supported.:" };
                throw err;
            }
            i++;
        }
    }

    function readStringViaCp() returns string {
        return cp.strings[readInt(channel)];
    }

    function readVariableDcl() returns BIRVariableDcl {
        var kind = readVarKind(channel);
        BIRVariableDcl dcl = {
            typeValue: readBType(),
            name: { value: readStringViaCp() },
            kind:kind
        };
        return dcl;
    }

    function readFunction() returns BIRFunction {
        var name = readStringViaCp();
        var isDeclaration = readBoolean(channel);
        var visibility = readVisibility(channel);
        var sig = readStringViaCp();
        var argsCount = readInt(channel);
        var numLocalVars = readInt(channel);

        BIRVariableDcl[] dcls;
        map<BIRVariableDcl> localVarMap;
        int i;
        while (i < numLocalVars) {
            var dcl = readVariableDcl();
            dcls[i] = dcl;
            localVarMap[dcl.name.value] = dcl;
            i++;
        }

        BIRBasicBlock[] basicBlocks;
        var numBB = readInt(channel);
        i = 0;
        while (i < numBB) {
            basicBlocks[i] = readBB(localVarMap);
            i++;
        }

        return {
            name: { value: name },
            isDeclaration: isDeclaration,
            visibility: visibility,
            localVars: dcls,
            basicBlocks: basicBlocks,
            argsCount: argsCount,
            typeValue: parseSig(sig)
        };
    }

    function readPackage() returns BIRPackage {
        var pkgIdCp = readInt(channel);
        var numFuncs = readInt(channel);
        BIRFunction[] funcs;
        int i;
        while (i < numFuncs) {
            funcs[i] = readFunction();
            i++;
        }
        return { functions:funcs };
    }

    function readBB(map<BIRVariableDcl> localVarMap) returns BIRBasicBlock {
        var id = readStringViaCp();
        var numInstruction = readInt(channel) - 1;
        BIRInstruction[] instructions;
        int i;
        while (i < numInstruction) {
            instructions[i] = readInstruction(localVarMap);
            i++;
        }
        return { id: { value: id },
            instructions: instructions,
            terminator: readTerminator(localVarMap) };
    }

    function readTerminator(map<BIRVariableDcl> localVarMap) returns BIRTerminator {
        var kindTag = readByte(channel);
        if (kindTag == 3){
            InstructionKind kind = "BRANCH";
            var op = readVarRef(localVarMap);
            BIRBasicBlock trueBB = readBBRef();
            BIRBasicBlock falseBB = readBBRef();
            return new Branch(falseBB, kind, op, trueBB);
        } else if (kindTag == 1){
            InstructionKind kind = "GOTO";
            return new GOTO(kind, readBBRef());
        } else if (kindTag == 4){
            InstructionKind kind = "RETURN";
            return new Return(kind);
        } else if (kindTag == 2){
            InstructionKind kind = "CALL";
            var pkgIdCp = readInt(channel);
            var name = readStringViaCp();
            var argsCount = readInt(channel);
            BIROperand[] args = [];
            int i = 0;
            while (i < argsCount) {
                args[i] = readVarRef(localVarMap);
                i++;
            }
            var hasLhs = readBoolean(channel);
            BIRVarRef? lhsOp = ();
            if (hasLhs){
                lhsOp = readVarRef(localVarMap);
            }
            BIRBasicBlock thenBB = readBBRef();
            return new Call(args, kind, lhsOp, { value: name }, thenBB);

        }
        error err = { message: "term instrucion kind " + kindTag + " not impl." };
        throw err;
    }

    function readBType() returns BType {
        string sginatureAlias = readStringViaCp();
        if (sginatureAlias == "I"){
            return "int";
        } else if (sginatureAlias == "B"){
            return "boolean";
        }
        error err = { message: "type signature " + sginatureAlias + " not supported." };
        throw err;
    }

    function readVarRef(map<BIRVariableDcl> localVarMap) returns BIRVarRef {
        var varName = readStringViaCp();
        var decl = getDecl(localVarMap, varName);
        return new BIRVarRef("VAR_REF", decl.typeValue, decl);
    }

    function readInstruction(map<BIRVariableDcl> localVarMap) returns BIRInstruction {
        var kindTag = readByte(channel);
        InstructionKind kind = "CONST_LOAD";
        // this is hacky to init to a fake val, but ballerina dosn't support un intialized vers
        if (kindTag == 6){
            //TODO: remove redundent
            var bType = readBType();
            kind = "CONST_LOAD";
            var constLoad = new ConstantLoad(kind,
                readVarRef(localVarMap),
                bType,
                readIntViaCp());
            return constLoad;
        } else if (kindTag == 5){
            kind = "MOVE";
            var rhsOp = readVarRef(localVarMap);
            var lhsOp = readVarRef(localVarMap);
            return new Move(kind, lhsOp, rhsOp);
        } else {
            return readBinaryOpInstruction(kindTag, localVarMap);
        }
    }

    function readBBRef() returns BIRBasicBlock {
        return { id: { value: readStringViaCp() } };
    }

    function readBinaryOpInstruction(int kindTag, map<BIRVariableDcl> localVarMap)
                 returns BinaryOp {
        BinaryOpInstructionKind kind = "ADD";
        if (kindTag == 7){
            kind = "ADD";
        } else if (kindTag == 8){
            kind = "SUB";
        } else if (kindTag == 9){
            kind = "MUL";
        } else if (kindTag == 10){
            kind = "DIV";
        } else if (kindTag == 12){
            kind = "EQUAL";
        } else if (kindTag == 13){
            kind = "NOT_EQUAL";
        } else if (kindTag == 14){
            kind = "GREATER_THAN";
        } else if (kindTag == 15){
            kind = "GREATER_EQUAL";
        } else if (kindTag == 16){
            kind = "LESS_THAN";
        } else if (kindTag == 17){
            kind = "LESS_EQUAL";
        } else {
            error err = { message: "instrucion kind " + kindTag + " not impl." };
            throw err;
        }

        var rhsOp1 = readVarRef(localVarMap);
        var rhsOp2 = readVarRef(localVarMap);
        var lhsOp = readVarRef(localVarMap);
        return new BinaryOp (kind, lhsOp, rhsOp1, rhsOp2,
            //TODO: remove type, not used
            "int");
    }

    function readIntViaCp() returns int {
        return cp.ints[readInt(channel)];
    }

};

function readBytes(io:ByteChannel channel, int numberOfBytes) returns (byte[], int) {
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
function writeBytes(io:ByteChannel channel, byte[] content, int startOffset = 0) returns int {
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
    error err = { message: "unknown var kind " + b };
    throw err;
}


function parseSig(string sig) returns BInvokableType {
    BType returnType = "int";
    //TODO: add boolean
    if (sig.lastIndexOf("(N)") == (lengthof sig - 3)){
        returnType = "()";
    }
    return {
        retType:returnType
    };
}

function getDecl(map<BIRVariableDcl> localVarMap, string varName) returns BIRVariableDcl {
    var posibalDcl = localVarMap[varName];
    match posibalDcl {
        BIRVariableDcl dcl => return dcl;
        () => {
            error err = { message: "local var missing " + varName};
            throw err;
        }
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


function readString(io:ByteChannel channel) returns string {
    var stringLen = untaint readInt(channel);
    if (stringLen > 0){
        var (strBytes, strLen) = check channel.read(untaint stringLen);
        return internal:byteArrayToString(strBytes, "UTF-8");
    } else {
        return "";
    }
}
