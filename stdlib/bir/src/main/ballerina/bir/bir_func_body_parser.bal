import ballerina/io;
import ballerina/internal;

public type BirFuncBodyParser object {
    BirChannelReader reader,
    map<BIRVariableDcl> localVarMap,
    public new(reader, localVarMap) {
    }

    public function parseBB() returns BIRBasicBlock {
        var id = reader.readStringCpRef();
        var numInstruction = reader.readInt32() - 1;
        BIRInstruction[] instructions;
        int i;
        while (i < numInstruction) {
            instructions[i] = parseInstruction();
            i++;
        }
        return { id: { value: id },
            instructions: instructions,
            terminator: parseTerminator() };
    }

    public function parseInstruction() returns BIRInstruction {
        var kindTag = reader.readInt8();
        InstructionKind kind = "CONST_LOAD";
        // this is hacky to init to a fake val, but ballerina dosn't support un intialized vers
        if (kindTag == 6){
            //TODO: remove redundent
            var bType = reader.readBType();
            kind = "CONST_LOAD";
            var constLoad = new ConstantLoad(kind,
                parseVarRef(),
                bType,
                reader.readIntCpRef());
            return constLoad;
        } else if (kindTag == 5){
            kind = "MOVE";
            var rhsOp = parseVarRef();
            var lhsOp = parseVarRef();
            return new Move(kind, lhsOp, rhsOp);
        } else {
            return parseBinaryOpInstruction(kindTag);
        }
    }



    public function parseTerminator() returns BIRTerminator {
        var kindTag = reader.readInt8();
        if (kindTag == 3){
            InstructionKind kind = "BRANCH";
            var op = parseVarRef();
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
            var name = reader.readStringCpRef();
            var argsCount = reader.readInt32();
            BIROperand[] args = [];
            int i = 0;
            while (i < argsCount) {
                args[i] = parseVarRef();
                i++;
            }
            var hasLhs = reader.readBoolean();
            BIRVarRef? lhsOp = ();
            if (hasLhs){
                lhsOp = parseVarRef();
            }
            BIRBasicBlock thenBB = parseBBRef();
            return new Call(args, kind, lhsOp, { value: name }, thenBB);

        }
        error err = { message: "term instrucion kind " + kindTag + " not impl." };
        throw err;
    }


    public function parseVarRef() returns BIRVarRef {
        var varName = reader.readStringCpRef();
        var decl = getDecl(localVarMap, varName);
        return new BIRVarRef("VAR_REF", decl.typeValue, decl);
    }

    public function parseBBRef() returns BIRBasicBlock {
        return { id: { value: reader.readStringCpRef() } };
    }

    public function parseBinaryOpInstruction(int kindTag) returns BinaryOp {
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

        var rhsOp1 = parseVarRef();
        var rhsOp2 = parseVarRef();
        var lhsOp = parseVarRef();
        return new BinaryOp (kind, lhsOp, rhsOp1, rhsOp2,
            //TODO: remove type, not used
            "int");
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

