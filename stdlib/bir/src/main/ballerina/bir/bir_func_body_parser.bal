// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/internal;
import ballerina/io;

public type FuncBodyParser object {
    BirChannelReader reader;
    TypeParser typeParser;
    map<VariableDcl> localVarMap;
    map<VariableDcl> globalVarMap;
    TypeDef?[] typeDefs;

    public function __init(BirChannelReader reader,  TypeParser typeParser, map<VariableDcl> globalVarMap, map<VariableDcl> localVarMap, TypeDef?[] typeDefs) {
        self.reader = reader;
        self.typeParser = typeParser;
        self.localVarMap = localVarMap;
        self.globalVarMap = globalVarMap;
        self.typeDefs = typeDefs;
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

        return { id: { value: id }, instructions: instructions, terminator: self.parseTerminator() };
    }

    public function parseEE() returns ErrorEntry {
        return { trapBB: self.parseBBRef(), errorOp: self.parseVarRef() };
    }

    public function parseInstruction() returns Instruction {
        var kindTag = self.reader.readInt8();
        InstructionKind kind = INS_KIND_CONST_LOAD;
        // this is hacky to init to a fake val, but ballerina dosn't support un intialized vars
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
            FieldAccess mapLoad = {kind:kind, lhsOp:lhsOp, keyOp:keyOp, rhsOp:rhsOp};
            return mapLoad;
        } else if (kindTag == INS_OBJECT_STORE) {
            kind = INS_KIND_OBJECT_STORE;
            var lhsOp = self.parseVarRef();
            var keyOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            FieldAccess objectStore = {kind:kind, lhsOp:lhsOp, keyOp:keyOp, rhsOp:rhsOp};
            return objectStore;
        } else if (kindTag == INS_OBJECT_LOAD) {
            kind = INS_KIND_OBJECT_LOAD;
            var lhsOp = self.parseVarRef();
            var keyOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            FieldAccess objectLoad = {kind:kind, lhsOp:lhsOp, keyOp:keyOp, rhsOp:rhsOp};
            return objectLoad;
        } else if (kindTag == INS_ARRAY_LOAD) {
            kind = INS_KIND_ARRAY_LOAD;
            var lhsOp = self.parseVarRef();
            var keyOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            FieldAccess arrayLoad = {kind:kind, lhsOp:lhsOp, keyOp:keyOp, rhsOp:rhsOp};
            return arrayLoad;
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
        } else if (kindTag == INS_NEW_INST) {
            var defIndex = self.reader.readInt32();
            kind = INS_KIND_NEW_INST;
            var lhsOp = self.parseVarRef();
            NewInstance newInst = {kind:kind, lhsOp:lhsOp, typeDef: self.findTypeDef(defIndex)};
            return newInst;
        } else if (kindTag == INS_TYPE_CAST) {
            kind = INS_KIND_TYPE_CAST;
            var lhsOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            TypeCast typeCast = {kind:kind, lhsOp:lhsOp, rhsOp:rhsOp};
            return typeCast;
        } else if (kindTag == INS_IS_LIKE) {
            kind = INS_KIND_IS_LIKE;
            var bType = self.typeParser.parseType();
            var lhsOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            IsLike isLike = {kind:kind, typeValue:bType, lhsOp:lhsOp, rhsOp:rhsOp};
            return isLike;
        } else if (kindTag == INS_TYPE_TEST) {
            kind = INS_KIND_TYPE_TEST;
            var bType = self.typeParser.parseType();
            var lhsOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            TypeTest typeTest = {kind:kind, typeValue:bType, lhsOp:lhsOp, rhsOp:rhsOp};
            return typeTest;
        } else if (kindTag == INS_CONST_LOAD){
            //TODO: remove redundent
            var bType = self.typeParser.parseType();
            kind = INS_KIND_CONST_LOAD;
            var lhsOp = self.parseVarRef();

            int | string | boolean | float value = 0;
            if (bType is BTypeInt || bType is BTypeByte) {
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
        } else if (kindTag == INS_FP_LOAD) {
            kind = INS_KIND_FP_LOAD;
            var lhsOp = self.parseVarRef();
            var pkgId = self.reader.readModuleIDCpRef();
            var name = self.reader.readStringCpRef();
            FPLoad fpLoad = {kind:kind, lhsOp:lhsOp, pkgID:pkgId, name:{ value: name }};
            return fpLoad;
        } else {
            return self.parseBinaryOpInstruction(kindTag);
        }
    }
    
    public function parseTerminator() returns Terminator {
        var kindTag = self.reader.readInt8();
        if (kindTag == INS_BRANCH){
            TerminatorKind kind = TERMINATOR_BRANCH;
            var op = self.parseVarRef();
            BasicBlock trueBB = self.parseBBRef();
            BasicBlock falseBB = self.parseBBRef();
            Branch branch = {falseBB:falseBB, kind:kind, op:op, trueBB:trueBB};
            return branch;
        } else if (kindTag == INS_GOTO){
            TerminatorKind kind = TERMINATOR_GOTO;
            GOTO goto = {kind:kind, targetBB:self.parseBBRef()};
            return goto;
        } else if (kindTag == INS_RETURN){
            TerminatorKind kind = TERMINATOR_RETURN;
            Return ret = {kind:kind};
            return ret;
        } else if (kindTag == INS_CALL){
            TerminatorKind kind = TERMINATOR_CALL;
            var isVirtual = self.reader.readBoolean();
            var pkgId = self.reader.readModuleIDCpRef();
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

            BasicBlock thenBB = self.parseBBRef();
            Call call = {args:args, kind:kind, isVirtual: isVirtual,
                         lhsOp:lhsOp, pkgID:pkgId,
                         name:{ value: name }, thenBB:thenBB};
            return call;
        } else if (kindTag == INS_ASYNC_CALL){
            TerminatorKind kind = TERMINATOR_ASYNC_CALL;
            var pkgId = self.reader.readModuleIDCpRef();
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

            BasicBlock thenBB = self.parseBBRef();
            AsyncCall call = {args:args, kind:kind, lhsOp:lhsOp, pkgID:pkgId, name:{ value: name }, thenBB:thenBB};
            return call;
        } else if (kindTag == INS_PANIC) {
            TerminatorKind kind = TERMINATOR_PANIC;
            var errorOp = self.parseVarRef();
            Panic panicStmt = { kind:kind, errorOp:errorOp };
            return panicStmt;
        }
        error err = error("term instrucion kind " + kindTag + " not impl.");
        panic err;
    }

    public function parseVarRef() returns VarRef {
        VarKind kind = parseVarKind(self.reader);
        VarScope varScope = parseVarScope(self.reader);
        string varName = self.reader.readStringCpRef();

        var decl = getDecl(self.globalVarMap, self.localVarMap, varScope, varName, kind);
        return {typeValue : decl.typeValue, variableDcl : decl};
    }

    public function parseBBRef() returns BasicBlock {
        return { id: { value: self.reader.readStringCpRef() } };
    }

    public function parseBinaryOpInstruction(int kindTag) returns BinaryOp {
        BinaryOpInstructionKind kind = BINARY_ADD;
        if (kindTag == INS_ADD){
            kind = BINARY_ADD;
        } else if (kindTag == INS_SUB){
            kind = BINARY_SUB;
        } else if (kindTag == INS_MUL){
            kind = BINARY_MUL;
        } else if (kindTag == INS_DIV){
            kind = BINARY_DIV;
        } else if (kindTag == INS_EQUAL){
            kind = BINARY_EQUAL;
        } else if (kindTag == INS_NOT_EQUAL){
            kind = BINARY_NOT_EQUAL;
        } else if (kindTag == INS_GREATER_THAN){
            kind = BINARY_GREATER_THAN;
        } else if (kindTag == INS_GREATER_EQUAL){
            kind = BINARY_GREATER_EQUAL;
        } else if (kindTag == INS_LESS_THAN){
            kind = BINARY_LESS_THAN;
        } else if (kindTag == INS_LESS_EQUAL){
            kind = BINARY_LESS_EQUAL;
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

    function findTypeDef(int defIndex) returns TypeDef {
        var typeDef = self.typeDefs[defIndex];
        if(typeDef is TypeDef){
            return typeDef;
        } else {
            error err = error("can't find type def for index : " + defIndex);
            panic err;
        }
    }

};

function getDecl(map<VariableDcl> globalVarMap, map<VariableDcl> localVarMap, VarScope varScope, string varName, 
        VarKind kind) returns VariableDcl {
    if (varScope == VAR_SCOPE_GLOBAL) {
        var possibleDcl = globalVarMap[varName];
        if (possibleDcl is VariableDcl) {
            return possibleDcl;
        } else {
            error err = error("global var missing " + varName);
            panic err;
        }
    }

    // for self referrence, create a dummy varDecl
    if (kind == VAR_KIND_SELF) {
        VariableDcl varDecl = { kind : kind, 
                                varScope : varScope, 
                                name : {value : varName}
                              };
        return varDecl;
    }

    var possibleDcl = localVarMap[varName];
    if (possibleDcl is VariableDcl) {
        return possibleDcl;
    } else {
        error err = error("local var missing " + varName);
        panic err;
    }
}

