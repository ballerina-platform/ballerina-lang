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

type BirBinParser object {
    ChannelReader reader,
    ConstPool cp;

    new(reader) {
    }

    function parseCp() {
        var cpCount = reader.readInt32();
        int i = 0;
        while (i < cpCount) {
            var cpType = reader.readInt8();
            if (cpType == 1){
                cp.ints[i] = reader.readInt64();
            } else if (cpType == 4){
                cp.strings[i] = reader.readString();
            } else if (cpType == 5){
                PackageId id = { org: cp.strings[reader.readInt32()],
                    name: <string>cp.strings[reader.readInt32()],
                    varstionVallue: <string>cp.strings[reader.readInt32()] };
                cp.packages[i] = id;
            } else {
                error err = { message: "cp type " + cpType + " not supported.:" };
                throw err;
            }
            i++;
        }
    }

    function parseVariableDcl() returns BIRVariableDcl {
        var kind = parseVarKind();
        BIRVariableDcl dcl = {
            typeValue: parseBType(),
            name: { value: parseStringCpRef() },
            kind:kind
        };
        return dcl;
    }

    function parseFunction() returns BIRFunction {
        var name = parseStringCpRef();
        var isDeclaration = reader.readBoolean();
        var visibility = parseVisibility();
        var sig = parseStringCpRef();
        var argsCount = reader.readInt32();
        var numLocalVars = reader.readInt32();

        BIRVariableDcl[] dcls;
        map<BIRVariableDcl> localVarMap;
        int i;
        while (i < numLocalVars) {
            var dcl = parseVariableDcl();
            dcls[i] = dcl;
            localVarMap[dcl.name.value] = dcl;
            i++;
        }

        BIRBasicBlock[] basicBlocks;
        var numBB = reader.readInt32();
        i = 0;
        while (i < numBB) {
            basicBlocks[i] = parseBB(localVarMap);
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

    function parsePackage() returns BIRPackage {
        var pkgIdCp = reader.readInt32();
        var numFuncs = reader.readInt32();
        BIRFunction[] funcs;
        int i;
        while (i < numFuncs) {
            funcs[i] = parseFunction();
            i++;
        }
        return { functions:funcs };
    }

    function parseBB(map<BIRVariableDcl> localVarMap) returns BIRBasicBlock {
        var id = parseStringCpRef();
        var numInstruction = reader.readInt32() - 1;
        BIRInstruction[] instructions;
        int i;
        while (i < numInstruction) {
            instructions[i] = parseInstruction(localVarMap);
            i++;
        }
        return { id: { value: id },
            instructions: instructions,
            terminator: parseTerminator(localVarMap) };
    }

    function parseTerminator(map<BIRVariableDcl> localVarMap) returns BIRTerminator {
        var kindTag = reader.readInt8();
        if (kindTag == 3){
            InstructionKind kind = "BRANCH";
            var op = parseVarRef(localVarMap);
            BIRBasicBlock trueBB = parseBBRef();
            BIRBasicBlock falseBB = parseBBRef();
            return new Branch(falseBB, kind, op, trueBB);
        } else if (kindTag == 1){
            InstructionKind kind = "GOTO";
            return new GOTO(kind, parseBBRef());
        } else if (kindTag == 4){
            InstructionKind kind = "RETURN";
            return new Return(kind);
        } else if (kindTag == 2){
            InstructionKind kind = "CALL";
            var pkgIdCp = reader.readInt32();
            var name = parseStringCpRef();
            var argsCount = reader.readInt32();
            BIROperand[] args = [];
            int i = 0;
            while (i < argsCount) {
                args[i] = parseVarRef(localVarMap);
                i++;
            }
            var hasLhs = reader.readBoolean();
            BIRVarRef? lhsOp = ();
            if (hasLhs){
                lhsOp = parseVarRef(localVarMap);
            }
            BIRBasicBlock thenBB = parseBBRef();
            return new Call(args, kind, lhsOp, { value: name }, thenBB);

        }
        error err = { message: "term instrucion kind " + kindTag + " not impl." };
        throw err;
    }

    function parseBType() returns BType {
        string sginatureAlias = parseStringCpRef();
        if (sginatureAlias == "I"){
            return "int";
        } else if (sginatureAlias == "B"){
            return "boolean";
        }
        error err = { message: "type signature " + sginatureAlias + " not supported." };
        throw err;
    }

    function parseVarRef(map<BIRVariableDcl> localVarMap) returns BIRVarRef {
        var varName = parseStringCpRef();
        var decl = getDecl(localVarMap, varName);
        return new BIRVarRef("VAR_REF", decl.typeValue, decl);
    }

    function parseInstruction(map<BIRVariableDcl> localVarMap) returns BIRInstruction {
        var kindTag = reader.readInt8();
        InstructionKind kind = "CONST_LOAD";
        // this is hacky to init to a fake val, but ballerina dosn't support un intialized vers
        if (kindTag == 6){
            //TODO: remove redundent
            var bType = parseBType();
            kind = "CONST_LOAD";
            var constLoad = new ConstantLoad(kind,
                parseVarRef(localVarMap),
                bType,
                parseIntCpRef());
            return constLoad;
        } else if (kindTag == 5){
            kind = "MOVE";
            var rhsOp = parseVarRef(localVarMap);
            var lhsOp = parseVarRef(localVarMap);
            return new Move(kind, lhsOp, rhsOp);
        } else {
            return parseBinaryOpInstruction(kindTag, localVarMap);
        }
    }

    function parseBBRef() returns BIRBasicBlock {
        return { id: { value: parseStringCpRef() } };
    }

    function parseBinaryOpInstruction(int kindTag, map<BIRVariableDcl> localVarMap) returns BinaryOp {
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

        var rhsOp1 = parseVarRef(localVarMap);
        var rhsOp2 = parseVarRef(localVarMap);
        var lhsOp = parseVarRef(localVarMap);
        return new BinaryOp (kind, lhsOp, rhsOp1, rhsOp2,
            //TODO: remove type, not used
            "int");
    }


    function parseVisibility() returns Visibility {
        int b = reader.readInt8();
        if (b == 0){
            return "PACKAGE_PRIVATE";
        } else if (b == 1){
            return "PRIVATE";
        } else if (b == 2){
            return "PUBLIC";
        }
        error err = { message: "unknown variable visiblity tag " + b };
        throw err;
    }

    function parseVarKind() returns VarKind {
        int b = reader.readInt8();
        if (b == 1){
            return "LOCAL";
        } else if (b == 2){
            return "ARG";
        } else if (b == 3){
            return "TEMP";
        } else if (b == 4){
            return "RETURN";
        }
        error err = { message: "unknown var kind tag " + b };
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

    function parseStringCpRef() returns string {
        return cp.strings[reader.readInt32()];
    }

    function parseIntCpRef() returns int {
        return cp.ints[reader.readInt32()];
    }

};


function getDecl(map<BIRVariableDcl> localVarMap, string varName) returns BIRVariableDcl {
    var posibalDcl = localVarMap[varName];
    match posibalDcl {
        BIRVariableDcl dcl => return dcl;
        () => {
            error err = { message: "local var missing " + varName };
            throw err;
        }
    }
}

