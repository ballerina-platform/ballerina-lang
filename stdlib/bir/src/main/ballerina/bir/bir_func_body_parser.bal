import ballerina/internal;

public type FuncBodyParser object {
    BirChannelReader reader;
    map<VariableDcl> localVarMap;
    public new(reader, localVarMap) {
    }

    public function parseBB() returns BasicBlock {
        var id = self.reader.readStringCpRef();
        var numInstruction = self.reader.readInt32() - 1;
        Instruction[] instructions = [];
        int i = 0;
        while (i < numInstruction) {
            instructions[i] = self.parseInstruction();
            i += 1;
        }
        return { id: { value: id },
            instructions: instructions,
            terminator: self.parseTerminator() };
    }

    public function parseInstruction() returns Instruction {
        var kindTag = self.reader.readInt8();
        InstructionKind kind = "CONST_LOAD";
        // this is hacky to init to a fake val, but ballerina dosn't support un intialized vers
        if (kindTag == 6){
            //TODO: remove redundent
            var bType = self.reader.readBType();
            kind = "CONST_LOAD";
            var constLoad = new ConstantLoad(kind,
                self.parseVarRef(),
                bType,
                self.reader.readIntCpRef());
            return constLoad;
        } else if (kindTag == 5){
            kind = "MOVE";
            var rhsOp = self.parseVarRef();
            var lhsOp = self.parseVarRef();
            return new Move(kind, lhsOp, rhsOp);
        } else {
            return self.parseBinaryOpInstruction(kindTag);
        }
    }



    public function parseTerminator() returns Terminator {
        var kindTag = self.reader.readInt8();
        if (kindTag == 3){
            InstructionKind kind = "BRANCH";
            var op = self.parseVarRef();
            BasicBlock trueBB = self.parseBBRef();
            BasicBlock falseBB = self.parseBBRef();
            return new Branch(falseBB, kind, op, trueBB);
        } else if (kindTag == 1){
            InstructionKind kind = "GOTO";
            return new GOTO(kind, self.parseBBRef());
        } else if (kindTag == 4){
            InstructionKind kind = "RETURN";
            return new Return(kind);
        } else if (kindTag == 2){
            InstructionKind kind = "CALL";
            var pkgIdCp = self.reader.readInt32();
            var name = self.reader.readStringCpRef();
            var argsCount = self.reader.readInt32();
            Operand[] args = [];
            int i = 0;
            while (i < argsCount) {
                args[i] = self.parseVarRef();
                i += 1;
            }
            var hasLhs = self.reader.readBoolean();
            VarRef? lhsOp = ();
            if (hasLhs){
                lhsOp = self.parseVarRef();
            }
            BasicBlock thenBB = self.parseBBRef();
            return new Call(args, kind, lhsOp, { value: name }, thenBB);

        }
        error err = error("term instrucion kind " + kindTag + " not impl.");
        panic err;
    }


    public function parseVarRef() returns VarRef {
        var varName = self.reader.readStringCpRef();
        var decl = getDecl(self.localVarMap, varName);
        return new VarRef("VAR_REF", decl.typeValue, decl);
    }

    public function parseBBRef() returns BasicBlock {
        return { id: { value: self.reader.readStringCpRef() } };
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
            error err = error("instrucion kind " + kindTag + " not impl.");
            panic err;
        }

        var rhsOp1 = self.parseVarRef();
        var rhsOp2 = self.parseVarRef();
        var lhsOp = self.parseVarRef();
        return new BinaryOp (kind, lhsOp, rhsOp1, rhsOp2,
            //TODO: remove type, not used
            "int");
    }

};

function getDecl(map<VariableDcl> localVarMap, string varName) returns VariableDcl {
    var posibalDcl = localVarMap[varName];
    match posibalDcl {
        VariableDcl dcl => return dcl;
        () => {
            error err = error("local var missing " + varName);
            panic err;
        }
    }
}

