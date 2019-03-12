import ballerina/internal;
import ballerina/io;

public type FuncBodyParser object {
    BirChannelReader reader;
    TypeParser typeParser;
    map<VariableDcl> localVarMap;
    map<VariableDcl> globalVarMap;
    public function __init(BirChannelReader reader,  TypeParser typeParser, map<VariableDcl> globalVarMap, map<VariableDcl> localVarMap) {
        self.reader = reader;
        self.typeParser = typeParser;
        self.localVarMap = localVarMap;
        self.globalVarMap = globalVarMap;
    }

    public function parseBB() returns BasicBlock {
        var id = self.reader.readStringCpRef();
        var numInstruction = self.reader.readInt32() - 1;
        Instruction?[] instructions = [];
        int i = 0;
        while (i < numInstruction) {
            instructions[i] = self.parseInstruction();
            i += 1;
        }

        var ins = Instruction[].stamp(instructions);
        if (ins is Instruction[]) {
            return { id: { value: id }, instructions: ins, terminator: self.parseTerminator() };
        } else {
            error err = error("error while parsing instructions");
            panic err;
        }
    }

    public function parseInstruction() returns Instruction {
        var kindTag = self.reader.readInt8();
        InstructionKind kind = INS_KIND_CONST_LOAD;
        // this is hacky to init to a fake val, but ballerina dosn't support un intialized vers
        if (kindTag == INS_ARRAY_STORE) {
            kind = INS_KIND_ARRAY_STORE;
            var lhsOp = self.parseVarRef();
            var keyOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            FieldAccess mapStore = {kind:kind, lhsOp:lhsOp, keyOp:keyOp, rhsOp:rhsOp};
            return mapStore;
        } else if (kindTag == INS_MAP_STORE) {
            kind = INS_KIND_MAP_STORE;
            var lhsOp = self.parseVarRef();
            var keyOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            FieldAccess mapStore = {kind:kind, lhsOp:lhsOp, keyOp:keyOp, rhsOp:rhsOp};
            return mapStore;
        } else if (kindTag == INS_MAP_LOAD) {
            kind = INS_KIND_MAP_LOAD;
            var lhsOp = self.parseVarRef();
            var keyOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            FieldAccess mapStore = {kind:kind, lhsOp:lhsOp, keyOp:keyOp, rhsOp:rhsOp};
            return mapStore;
        } else if (kindTag == INS_ARRAY_LOAD) {
            kind = INS_KIND_ARRAY_LOAD;
            var lhsOp = self.parseVarRef();
            var keyOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            FieldAccess mapStore = {kind:kind, lhsOp:lhsOp, keyOp:keyOp, rhsOp:rhsOp};
            return mapStore;
        } else if (kindTag == INS_NEW_ARRAY) {
            var bType = self.typeParser.parseType();
            kind = INS_KIND_NEW_ARRAY;
            var lhsOp = self.parseVarRef();
            var sizeOp = self.parseVarRef();
            NewArray newArray = {kind:kind, lhsOp:lhsOp, sizeOp:sizeOp, typeValue:bType};
            return newArray;
        } else if (kindTag == INS_NEW_MAP) {
            var bType = self.typeParser.parseType();
            kind = INS_KIND_NEW_MAP;
            var lhsOp = self.parseVarRef();
            NewMap newMap = {kind:kind, lhsOp:lhsOp, typeValue:bType};
            return newMap;
        } else if (kindTag == INS_TYPE_CAST) {
            kind = INS_KIND_TYPE_CAST;
            var lhsOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            TypeCast typeCast = {kind:kind, lhsOp:lhsOp, rhsOp:rhsOp};
            return typeCast;
        } else if (kindTag == INS_CONST_LOAD){
            //TODO: remove redundent
            var bType = self.typeParser.parseType();
            kind = INS_KIND_CONST_LOAD;
            var lhsOp = self.parseVarRef();

            int | string | boolean | float value = 0;
            if (bType is BTypeInt) {
                value = self.reader.readIntCpRef();
            } else if (bType is BTypeString) {
                value = self.reader.readStringCpRef();
            } else if (bType is BTypeBoolean) {
                value = self.reader.readBoolean();
            } else if (bType is BTypeFloat) {
                value = self.reader.readFloatCpRef();
            }
            ConstantLoad constLoad = {kind:kind, lhsOp:lhsOp, typeValue:bType, value:value};
            return constLoad;
        } else if (kindTag == INS_MOVE){
            kind = INS_KIND_MOVE;
            var rhsOp = self.parseVarRef();
            var lhsOp = self.parseVarRef();
            Move move = {kind:kind, lhsOp:lhsOp, rhsOp:rhsOp};
            return move;
        } else if (kindTag == INS_NEW_ERROR) {
            kind = INS_KIND_NEW_ERROR;
            var lhsOp = self.parseVarRef();
            var reasonOp = self.parseVarRef();
            var detailsOp = self.parseVarRef();
            NewError newError = {kind:kind, lhsOp:lhsOp, reasonOp:reasonOp, detailsOp:detailsOp};
            return newError;
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
            var pkgId = self.reader.readPackageIdCpRef();
            var name = self.reader.readStringCpRef();
            var argsCount = self.reader.readInt32();
            VarRef?[] args = [];
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

            var result = VarRef[].stamp(args);
            if (result is VarRef[]) {
                BasicBlock thenBB = self.parseBBRef();
                Call call = {args:result, kind:kind, lhsOp:lhsOp, pkgID:pkgId, name:{ value: name }, thenBB:thenBB};
                return call;
            } else {
                error err = error("error while parsing args");
                panic err;
            }

        }
        error err = error("term instrucion kind " + kindTag + " not impl.");
        panic err;
    }


    public function parseVarRef() returns VarRef {
        var kind = parseVarKind(self.reader);
        var varName = self.reader.readStringCpRef();
        var decl = getDecl(self.globalVarMap, self.localVarMap, kind, varName);
        return {kind : "VAR_REF", typeValue : decl.typeValue, variableDcl : decl};
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

function getDecl(map<VariableDcl> globalVarMap, map<VariableDcl> localVarMap, VarKind kind, string varName) returns VariableDcl {
    if (kind is GlobalVarKind) {
        var posibalDcl = globalVarMap[varName];
        if (posibalDcl is VariableDcl) {
            return posibalDcl;
        } else {
            error err = error("local var missing " + varName);
            panic err;
        }
    }
    var posibalDcl = localVarMap[varName];
    if (posibalDcl is VariableDcl) {
        return posibalDcl;
    } else {
        error err = error("local var missing " + varName);
        panic err;
    }
}

