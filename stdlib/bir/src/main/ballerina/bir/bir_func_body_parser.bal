import ballerina/internal;

public type FuncBodyParser object {
    BirChannelReader reader;
    TypeParser typeParser;
    map<VariableDcl> localVarMap;
    public function __init(BirChannelReader reader,  TypeParser typeParser, map<VariableDcl> localVarMap) {
        self.reader = reader;
        self.typeParser = typeParser;
        self.localVarMap = localVarMap;
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
        if (kindTag == INS_MAP_STORE) {
            var bType = self.typeParser.parseType();
            kind = "MAP_STORE";
            var lhsOp = self.parseVarRef();
            var keyOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            MapStore mapStore = {kind:kind, lhsOp:lhsOp, typeValue:bType, keyOp:keyOp, rhsOp:rhsOp};
            return mapStore;
        } else if (kindTag == INS_NEW_MAP) {
            var bType = self.typeParser.parseType();
            kind = "NEW_MAP";
            var lhsOp = self.parseVarRef();
            NewMap newMap = {kind:kind, lhsOp:lhsOp, typeValue:bType};
            return newMap;
        } else if (kindTag == INS_CONST_LOAD){
            //TODO: remove redundent
            var bType = self.typeParser.parseType();
            kind = "CONST_LOAD";
            var lhsOp = self.parseVarRef();

            int | string value = 0;
            if (bType is BTypeInt) {
                value = self.reader.readIntCpRef();
            } else if (bType is BTypeString) {
                value = self.reader.readStringCpRef();
            }
            ConstantLoad constLoad = {kind:kind, lhsOp:lhsOp, typeValue:bType, value:value};
            return constLoad;
        } else if (kindTag == INS_MOVE){
            kind = "MOVE";
            var rhsOp = self.parseVarRef();
            var lhsOp = self.parseVarRef();
            Move move = {kind:kind, lhsOp:lhsOp, rhsOp:rhsOp};
            return move;
        } else {
            return self.parseBinaryOpInstruction(kindTag);
        }
    }



    public function parseTerminator() returns Terminator {
        var kindTag = self.reader.readInt8();
        if (kindTag == INS_BRANCH){
            TerminatorKind kind = "BRANCH";
            var op = self.parseVarRef();
            BasicBlock trueBB = self.parseBBRef();
            BasicBlock falseBB = self.parseBBRef();
            Branch branch = {falseBB:falseBB, kind:kind, op:op, trueBB:trueBB};
            return branch;
        } else if (kindTag == INS_GOTO){
            TerminatorKind kind = "GOTO";
            GOTO goto = {kind:kind, targetBB:self.parseBBRef()};
            return goto;
        } else if (kindTag == INS_RETURN){
            TerminatorKind kind = "RETURN";
            Return ret = {kind:kind};
            return ret;
        } else if (kindTag == INS_CALL){
            TerminatorKind kind = "CALL";
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
            Call call = {args:args, kind:kind, lhsOp:lhsOp, name:{ value: name }, thenBB:thenBB};
            return call;

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
        if (kindTag == INS_ADD){
            kind = "ADD";
        } else if (kindTag == INS_SUB){
            kind = "SUB";
        } else if (kindTag == INS_MUL){
            kind = "MUL";
        } else if (kindTag == INS_DIV){
            kind = "DIV";
        } else if (kindTag == INS_EQUAL){
            kind = "EQUAL";
        } else if (kindTag == INS_NOT_EQUAL){
            kind = "NOT_EQUAL";
        } else if (kindTag == INS_GREATER_THAN){
            kind = "GREATER_THAN";
        } else if (kindTag == INS_GREATER_EQUAL){
            kind = "GREATER_EQUAL";
        } else if (kindTag == INS_LESS_THAN){
            kind = "LESS_THAN";
        } else if (kindTag == INS_LESS_EQUAL){
            kind = "LESS_EQUAL";
        } else {
            error err = error("instrucion kind " + kindTag + " not impl.");
            panic err;
        }

        var rhsOp1 = self.parseVarRef();
        var rhsOp2 = self.parseVarRef();
        var lhsOp = self.parseVarRef();
        BinaryOp binaryOp = {kind:kind, lhsOp:lhsOp, rhsOp1:rhsOp1, rhsOp2:rhsOp2};
        return binaryOp;
    }

};

function getDecl(map<VariableDcl> localVarMap, string varName) returns VariableDcl {
    var posibalDcl = localVarMap[varName];
    if (posibalDcl is VariableDcl) {
        return posibalDcl;
    } else {
        error err = error("local var missing " + varName);
        panic err;
    }
}

