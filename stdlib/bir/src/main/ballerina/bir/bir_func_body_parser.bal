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
    map<VariableDcl> localVarMap;
    map<VariableDcl> globalVarMap;
    TypeDef?[] typeDefs;

    public function __init(BirChannelReader reader, map<VariableDcl> globalVarMap, map<VariableDcl> localVarMap, TypeDef?[] typeDefs) {
        self.reader = reader;
        self.localVarMap = localVarMap;
        self.globalVarMap = globalVarMap;
        self.typeDefs = typeDefs;
    }

    public function parseBB(boolean addInterimBB) returns BasicBlock[] {
        var id = self.reader.readStringCpRef();
        var numInstruction = self.reader.readInt32() - 1;
        Instruction?[] instructions = [];
        int i = 0;
        while (i < numInstruction) {
            instructions[i] = self.parseInstruction();
            i += 1;
        }

        // Need to make sure terminators are at top of each switch case to avoid side effects due to reschedules.
        if (addInterimBB) {
            Terminator terminator = self.parseTerminator();
            BasicBlock interimBB = {id: { value: id + "interim" }, instructions: [], terminator:terminator };
            Terminator goto = self.createGOTO(terminator.pos, interimBB);
            BasicBlock initialBB = { id: { value: id }, instructions: instructions, terminator: goto };
            BasicBlock[2] bbs = [initialBB, interimBB];
            return bbs;
        }

        BasicBlock[1] bb = [{ id: { value: id }, instructions: instructions, terminator: self.parseTerminator() }];
        return bb;
    }

    private function createGOTO(DiagnosticPos pos, BasicBlock thenBB) returns Terminator {
        TerminatorKind kind = TERMINATOR_GOTO;
        GOTO goto = {pos:pos, kind:kind, targetBB:thenBB};
        return goto;
    }

    public function parseEE() returns ErrorEntry {
        return { trapBB: self.parseBBRef(), errorOp: self.parseVarRef() };
    }

    public function parseInstruction() returns Instruction {
        DiagnosticPos pos = parseDiagnosticPos(self.reader);
        var kindTag = self.reader.readInt8();
        InstructionKind kind = INS_KIND_CONST_LOAD;
        // this is hacky to init to a fake val, but ballerina dosn't support un intialized vars
        if (kindTag == INS_ARRAY_STORE) {
            kind = INS_KIND_ARRAY_STORE;
            var lhsOp = self.parseVarRef();
            var keyOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            FieldAccess mapStore = {pos:pos, kind:kind, lhsOp:lhsOp, keyOp:keyOp, rhsOp:rhsOp};
            return mapStore;
        } else if (kindTag == INS_MAP_STORE) {
            kind = INS_KIND_MAP_STORE;
            var lhsOp = self.parseVarRef();
            var keyOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            FieldAccess mapStore = {pos:pos, kind:kind, lhsOp:lhsOp, keyOp:keyOp, rhsOp:rhsOp};
            return mapStore;
        } else if (kindTag == INS_MAP_LOAD) {
            kind = INS_KIND_MAP_LOAD;
            boolean except = self.reader.readBoolean();
            var lhsOp = self.parseVarRef();
            var keyOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            FieldAccess mapLoad = {pos:pos, kind:kind, lhsOp:lhsOp, keyOp:keyOp, rhsOp:rhsOp, except:except};
            return mapLoad;
        } else if (kindTag == INS_OBJECT_STORE) {
            kind = INS_KIND_OBJECT_STORE;
            var lhsOp = self.parseVarRef();
            var keyOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            FieldAccess objectStore = {pos:pos, kind:kind, lhsOp:lhsOp, keyOp:keyOp, rhsOp:rhsOp};
            return objectStore;
        } else if (kindTag == INS_OBJECT_LOAD) {
            kind = INS_KIND_OBJECT_LOAD;
            var lhsOp = self.parseVarRef();
            var keyOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            FieldAccess objectLoad = {pos:pos, kind:kind, lhsOp:lhsOp, keyOp:keyOp, rhsOp:rhsOp};
            return objectLoad;
        } else if (kindTag == INS_ARRAY_LOAD) {
            kind = INS_KIND_ARRAY_LOAD;
            var lhsOp = self.parseVarRef();
            var keyOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            FieldAccess arrayLoad = {pos:pos, kind:kind, lhsOp:lhsOp, keyOp:keyOp, rhsOp:rhsOp};
            return arrayLoad;
        } else if (kindTag == INS_NEW_ARRAY) {
            var bType = self.reader.readTypeCpRef();
            kind = INS_KIND_NEW_ARRAY;
            var lhsOp = self.parseVarRef();
            var sizeOp = self.parseVarRef();
            NewArray newArray = {pos:pos, kind:kind, lhsOp:lhsOp, sizeOp:sizeOp, typeValue:bType};
            return newArray;
        } else if (kindTag == INS_NEW_MAP) {
            var bType = self.reader.readTypeCpRef();
            kind = INS_KIND_NEW_MAP;
            TypeRef? typeRef = self.parseRecordTypeRef();
            var lhsOp = self.parseVarRef();
            NewMap newMap = {pos:pos, kind:kind, lhsOp:lhsOp, typeRef:typeRef, bType:bType};
            return newMap;
        } else if (kindTag == INS_NEW_STREAM) {
            var bType = self.reader.readTypeCpRef();
            kind = INS_KIND_NEW_STREAM;
            var lhsOp = self.parseVarRef();
            var nameOp = self.parseVarRef();
            NewStream newStream = { pos: pos, kind: kind, lhsOp: lhsOp, nameOp: nameOp, typeValue: bType };
            return newStream;
        } else if (kindTag == INS_NEW_TABLE) {
            var bType = self.reader.readTypeCpRef();
            kind = INS_KIND_NEW_TABLE;
            var lhsOp = self.parseVarRef();
            var columnsOp = self.parseVarRef();
            var dataOp = self.parseVarRef();
            var indexColOp = self.parseVarRef();
            var keyColOp = self.parseVarRef();
            NewTable newTable = { pos: pos, kind: kind, lhsOp: lhsOp, columnsOp: columnsOp, dataOp: dataOp, indexColOp:
            indexColOp, keyColOp: keyColOp, typeValue: bType };
            return newTable;
        } else if (kindTag == INS_NEW_INST) {
            kind = INS_KIND_NEW_INST;
            NewInstance newInst = {pos:pos, kind:kind, typeDefRef: self.parseTypeDefRef(), lhsOp: self.parseVarRef()};
            return newInst;
        } else if (kindTag == INS_TYPE_CAST) {
            kind = INS_KIND_TYPE_CAST;
            var lhsOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            TypeCast typeCast = {pos:pos, kind:kind, lhsOp:lhsOp, rhsOp:rhsOp};
            return typeCast;
        } else if (kindTag == INS_IS_LIKE) {
            kind = INS_KIND_IS_LIKE;
            var bType = self.reader.readTypeCpRef();
            var lhsOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            IsLike isLike = {pos:pos, kind:kind, typeValue:bType, lhsOp:lhsOp, rhsOp:rhsOp};
            return isLike;
        } else if (kindTag == INS_TYPE_TEST) {
            kind = INS_KIND_TYPE_TEST;
            var bType = self.reader.readTypeCpRef();
            var lhsOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            TypeTest typeTest = {pos:pos, kind:kind, typeValue:bType, lhsOp:lhsOp, rhsOp:rhsOp};
            return typeTest;
        } else if (kindTag == INS_CONST_LOAD){
            //TODO: remove redundent
            var bType = self.reader.readTypeCpRef();
            kind = INS_KIND_CONST_LOAD;
            var lhsOp = self.parseVarRef();

            int | string | boolean | float | byte value = 0;
            if (bType is BTypeInt) {
                value = self.reader.readIntCpRef();
            } else if (bType is BTypeByte) {
                value = self.reader.readByteCpRef();
            } else if (bType is BTypeString) {
                value = self.reader.readStringCpRef();
            } else if (bType is BTypeDecimal) {
                value = self.reader.readStringCpRef();
            } else if (bType is BTypeBoolean) {
                value = self.reader.readBoolean();
            } else if (bType is BTypeFloat) {
                value = self.reader.readFloatCpRef();
            }
            ConstantLoad constLoad = {pos:pos, kind:kind, lhsOp:lhsOp, typeValue:bType, value:value};
            return constLoad;
        } else if (kindTag == INS_MOVE){
            kind = INS_KIND_MOVE;
            var rhsOp = self.parseVarRef();
            var lhsOp = self.parseVarRef();
            Move move = {pos:pos, kind:kind, lhsOp:lhsOp, rhsOp:rhsOp};
            return move;
        } else if (kindTag == INS_NEW_ERROR) {
            kind = INS_KIND_NEW_ERROR;
            var typeValue = self.reader.readTypeCpRef();
            var lhsOp = self.parseVarRef();
            var reasonOp = self.parseVarRef();
            var detailsOp = self.parseVarRef();
            NewError newError = {pos:pos, kind:kind, typeValue:typeValue, lhsOp:lhsOp, reasonOp:reasonOp, 
                                 detailsOp:detailsOp};
            return newError;
        } else if (kindTag == INS_NEW_XML_ELEMENT) {
            kind = INS_KIND_NEW_XML_ELEMENT;
            var lhsOp = self.parseVarRef();
            var startTagOp = self.parseVarRef();
            var endTagOp = self.parseVarRef();
            var defaultNsURIOp = self.parseVarRef();
            NewXMLElement newXMLElement = {pos:pos, kind:kind, lhsOp:lhsOp, startTagOp:startTagOp, endTagOp:endTagOp, 
                                           defaultNsURIOp:defaultNsURIOp};
            return newXMLElement;
        } else if (kindTag == INS_NEW_XML_TEXT) {
            kind = INS_KIND_NEW_XML_TEXT;
            var lhsOp = self.parseVarRef();
            var textOp = self.parseVarRef();
            NewXMLText newXMLText = {pos:pos, kind:kind, lhsOp:lhsOp, textOp:textOp};
            return newXMLText;
        } else if (kindTag == INS_NEW_XML_COMMENT) {
            kind = INS_KIND_NEW_XML_COMMENT;
            var lhsOp = self.parseVarRef();
            var textOp = self.parseVarRef();
            NewXMLComment newXMLComment = {pos:pos, kind:kind, lhsOp:lhsOp, textOp:textOp};
            return newXMLComment;
        } else if (kindTag == INS_NEW_XML_PI) {
            kind = INS_KIND_NEW_XML_PI;
            var lhsOp = self.parseVarRef();
            var dataOp = self.parseVarRef();
            var targetOp = self.parseVarRef();
            NewXMLPI newXMLPI = {pos:pos, kind:kind, lhsOp:lhsOp, dataOp:dataOp, targetOp:targetOp};
            return newXMLPI;
        } else if (kindTag == INS_NEW_XML_QNAME) {
            kind = INS_KIND_NEW_XML_QNAME;
            var lhsOp = self.parseVarRef();
            var localnameOp = self.parseVarRef();
            var nsURIOp = self.parseVarRef();
            var prefixOp = self.parseVarRef();
            NewXMLQName newXMLQName = {pos:pos, kind:kind, lhsOp:lhsOp, localnameOp:localnameOp, nsURIOp:nsURIOp, 
                                       prefixOp:prefixOp};
            return newXMLQName;
        } else if (kindTag == INS_NEW_STRING_XML_QNAME) {
            kind = INS_KIND_NEW_STRING_XML_QNAME;
            var lhsOp = self.parseVarRef();
            var stringQNameOp = self.parseVarRef();
            NewStringXMLQName newStringXMLQName = {pos:pos, kind:kind, lhsOp:lhsOp, stringQNameOp:stringQNameOp};
            return newStringXMLQName;
        } else if (kindTag == INS_XML_SEQ_STORE) {
            kind = INS_KIND_XML_SEQ_STORE;
            var lhsOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            XMLAccess xmlSeqStore = {pos:pos, kind:kind, lhsOp:lhsOp, rhsOp:rhsOp};
            return xmlSeqStore;
        } else if (kindTag == INS_XML_SEQ_LOAD) {
            kind = INS_KIND_XML_SEQ_LOAD;
            var lhsOp = self.parseVarRef();
            var keyOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            FieldAccess xmlLoad = {pos:pos, kind:kind, lhsOp:lhsOp, keyOp:keyOp, rhsOp:rhsOp};
            return xmlLoad;
        } else if (kindTag == INS_XML_LOAD) {
            kind = INS_KIND_XML_LOAD;
            var lhsOp = self.parseVarRef();
            var keyOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            FieldAccess xmlLoad = {pos:pos, kind:kind, lhsOp:lhsOp, keyOp:keyOp, rhsOp:rhsOp};
            return xmlLoad;
        } else if (kindTag == INS_XML_LOAD_ALL) {
            kind = INS_KIND_XML_LOAD_ALL;
            var lhsOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            XMLAccess xmlLoadAll = {pos:pos, kind:kind, lhsOp:lhsOp, rhsOp:rhsOp};
            return xmlLoadAll;
        } else if (kindTag == INS_XML_ATTRIBUTE_STORE) {
            kind = INS_KIND_XML_ATTRIBUTE_STORE;
            var lhsOp = self.parseVarRef();
            var keyOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            FieldAccess xmlAttrStore = {pos:pos, kind:kind, lhsOp:lhsOp, keyOp:keyOp, rhsOp:rhsOp};
            return xmlAttrStore;
        } else if (kindTag == INS_XML_ATTRIBUTE_LOAD) {
            kind = INS_KIND_XML_ATTRIBUTE_LOAD;
            var lhsOp = self.parseVarRef();
            var keyOp = self.parseVarRef();
            var rhsOp = self.parseVarRef();
            FieldAccess xmlAttrLoad = {pos:pos, kind:kind, lhsOp:lhsOp, keyOp:keyOp, rhsOp:rhsOp};
            return xmlAttrLoad;
        } else if (kindTag == INS_FP_LOAD) {
            kind = INS_KIND_FP_LOAD;
            var lhsOp = self.parseVarRef();
            var pkgId = self.reader.readModuleIDCpRef();
            var name = self.reader.readStringCpRef();

            var mapCount = self.reader.readInt32();
            VarRef?[] maps = [];
            int j = 0;
            while (j < mapCount) {
                maps[j] = self.parseVarRef();
                j += 1;
            }

            VariableDcl?[] params = [];
            var numVars = self.reader.readInt32();
            int i = 0;
            while (i < numVars) {
                var dcl = parseVariableDcl(self.reader);
                params[i] = dcl;
                i += 1;
            }
            FPLoad fpLoad = {pos:pos, kind:kind, lhsOp:lhsOp, pkgID:pkgId, name:{ value: name }, params:params, closureMaps:maps};
            return fpLoad;
        } else if (kindTag == INS_TYPEOF) {
            kind = INS_KIND_TYPEOF;
            var rhsOp = self.parseVarRef();
            var lhsOp = self.parseVarRef();
            UnaryOp typeofNode = {pos:pos, kind:kind, lhsOp:lhsOp, rhsOp:rhsOp};
            return typeofNode;
        } else if (kindTag == INS_NOT) {
            kind = INS_KIND_NOT;
            var rhsOp = self.parseVarRef();
            var lhsOp = self.parseVarRef();
            UnaryOp typeofNode = {pos:pos, kind:kind, lhsOp:lhsOp, rhsOp:rhsOp};
            return typeofNode;
        } else if (kindTag == INS_NEW_TYPEDESC) {
            kind = INS_KIND_NEW_TYPEDESC;
            var lhsOp = self.parseVarRef();
            var bType = self.reader.readTypeCpRef();
            NewTypeDesc newTypeDesc = {pos:pos, kind:kind, lhsOp:lhsOp, typeValue:bType};
            return newTypeDesc;
        } else if (kindTag == INS_NEGATE) {
            kind = INS_KIND_NEGATE;
            var rhsOp = self.parseVarRef();
            var lhsOp = self.parseVarRef();
            UnaryOp typeofNode = {pos:pos, kind:kind, lhsOp:lhsOp, rhsOp:rhsOp};
            return typeofNode;
        } else {
            return self.parseBinaryOpInstruction(kindTag, pos);
        }
    }

    public function parseTypeDefRef() returns TypeDef|TypeRef {
        var isExternalDef = self.reader.readBoolean();
        if (isExternalDef) {
            TypeRef typeRef = {externalPkg: self.reader.readModuleIDCpRef(),
                               name: {value: self.reader.readStringCpRef()}};
            return typeRef;
        } else {
            return self.findTypeDef(self.reader.readInt32());
        }
    }

    public function parseRecordTypeRef() returns TypeRef? {
        var isExternalDef = self.reader.readBoolean();
        if (isExternalDef) {
            TypeRef typeRef = {externalPkg: self.reader.readModuleIDCpRef(),
                               name: {value: self.reader.readStringCpRef()}};
            return typeRef;
        }

        return;
    }

    public function parseTerminator() returns Terminator {
        DiagnosticPos pos = parseDiagnosticPos(self.reader);
        var kindTag = self.reader.readInt8();
        if (kindTag == INS_BRANCH){
            TerminatorKind kind = TERMINATOR_BRANCH;
            var op = self.parseVarRef();
            BasicBlock trueBB = self.parseBBRef();
            BasicBlock falseBB = self.parseBBRef();
            Branch branch = {pos:pos, falseBB:falseBB, kind:kind, op:op, trueBB:trueBB};
            return branch;
        } else if (kindTag == INS_GOTO){
            TerminatorKind kind = TERMINATOR_GOTO;
            GOTO goto = {pos:pos, kind:kind, targetBB:self.parseBBRef()};
            return goto;
        } else if (kindTag == INS_RETURN){
            TerminatorKind kind = TERMINATOR_RETURN;
            Return ret = {pos:pos, kind:kind};
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
            Call call = {pos:pos, args:args, kind:kind, isVirtual: isVirtual,
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
            AsyncCall call = {pos:pos, args:args, kind:kind, lhsOp:lhsOp, pkgID:pkgId, 
                                name:{ value: name }, thenBB:thenBB};
            return call;
        } else if (kindTag == INS_PANIC) {
            TerminatorKind kind = TERMINATOR_PANIC;
            var errorOp = self.parseVarRef();
            Panic panicStmt = { pos:pos, kind:kind, errorOp:errorOp };
            return panicStmt;
        } else if (kindTag == INS_WAIT) {
            TerminatorKind kind = TERMINATOR_WAIT;
            var exprCount = self.reader.readInt32();
            VarRef?[] exprs = [];
            int i = 0;
            while (i < exprCount) {
                exprs[i] = self.parseVarRef();
                i += 1;
            }
            VarRef lhsOp = self.parseVarRef();
            BasicBlock thenBB = self.parseBBRef();
            Wait waitIns = {pos:pos, exprList:exprs, kind:kind, lhsOp:lhsOp, thenBB:thenBB};
            return waitIns;
        } else if (kindTag == INS_WAIT_ALL) {
            TerminatorKind kind = TERMINATOR_WAIT_ALL;
            var lhsOp = self.parseVarRef();
            int length = self.reader.readInt32();
            string[] keys = [];
            int futureIndex = 0;
            while (futureIndex < length) {
                keys[futureIndex] = self.reader.readStringCpRef();
                futureIndex += 1;
            }
            futureIndex = 0;
            VarRef?[] futures = [];
            while (futureIndex < length) {
                futures[futureIndex] = self.parseVarRef();
                futureIndex += 1;
            }
            BasicBlock thenBB = self.parseBBRef();
            WaitAll waitAll = {pos:pos, kind:kind, keys:keys, futures:futures, lhsOp:lhsOp, thenBB:thenBB};
            return waitAll;
        } else if (kindTag == INS_FLUSH) {
            TerminatorKind kind = TERMINATOR_FLUSH;
            ChannelDetail[] channels = getWorkerChannels(self.reader);
            VarRef lhsOp = self.parseVarRef();
            BasicBlock thenBB = self.parseBBRef();
            Flush flushIns = {pos:pos, workerChannels:channels, kind:kind, lhsOp:lhsOp, thenBB:thenBB};
            return flushIns;
        } else if (kindTag == INS_FP_CALL) {
            TerminatorKind kind = TERMINATOR_FP_CALL;
            VarRef fp = self.parseVarRef();
            
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
            var isAsync = self.reader.readBoolean();
            BasicBlock thenBB = self.parseBBRef();
            FPCall fpCall = {pos:pos, kind:kind, fp:fp, lhsOp:lhsOp, args:args, thenBB:thenBB, isAsync:isAsync};
            return fpCall;
        } else if (kindTag == INS_WK_RECEIVE) {
            TerminatorKind kind = TERMINATOR_WK_RECEIVE;
            string dataChannel = self.reader.readStringCpRef();
            VarRef lhsOp = self.parseVarRef();
            boolean isSameStrand = self.reader.readBoolean();
            BasicBlock thenBB = self.parseBBRef();
            WorkerReceive receive = {pos:pos, kind:kind, channelName:{ value:dataChannel }, lhsOp:lhsOp,
                isSameStrand:isSameStrand, thenBB:thenBB};
            return receive;
        } else if (kindTag == INS_WK_SEND) {
            TerminatorKind kind = TERMINATOR_WK_SEND;
            string dataChannel = self.reader.readStringCpRef();
            VarRef dataOp = self.parseVarRef();
            boolean isSameStrand = self.reader.readBoolean();
            boolean isSync = self.reader.readBoolean();
            VarRef? lhsOp = ();
            if (isSync) {
                lhsOp = self.parseVarRef();
            }
            BasicBlock thenBB = self.parseBBRef();
            WorkerSend send = {pos:pos, kind:kind, channelName:{ value:dataChannel }, dataOp:dataOp,
                isSameStrand:isSameStrand, isSync:isSync, lhsOp:lhsOp, thenBB:thenBB};
            return send;
        } else if (kindTag == INS_LOCK) {
            TerminatorKind kind = TERMINATOR_LOCK;

            var globleVarCount = self.reader.readInt32();
            string[] globleVarName = [];
            int i = 0;
            while (i < globleVarCount) {
                globleVarName[i] = self.reader.readStringCpRef();
                i += 1;
            }

            Lock lockIns = {pos:pos, kind:kind, globleVars:globleVarName, lockBB:self.parseBBRef()};
            return lockIns;
        } else if (kindTag == INS_UNLOCK) {
            TerminatorKind kind = TERMINATOR_UNLOCK;

            var globleVarCount = self.reader.readInt32();
            string[] globleVarName = [];
            int i = 0;
            while (i < globleVarCount) {
                globleVarName[i] = self.reader.readStringCpRef();
                i += 1;
            }

            Unlock unlockIns = {pos:pos, kind:kind, globleVars:globleVarName, unlockBB:self.parseBBRef()};
            return unlockIns;
        }
        error err = error("term instruction kind " + kindTag + " not impl.");
        panic err;
    }

    public function parseVarRef() returns VarRef {
        boolean ignoreVariable = self.reader.readBoolean();
        if (ignoreVariable) {
            var bType = self.reader.readTypeCpRef();
            VariableDcl decl = { kind: VAR_KIND_ARG, varScope: VAR_SCOPE_FUNCTION, name: { value: "_" } };
            return { typeValue: bType, variableDcl: decl };
        }

        VarKind kind = parseVarKind(self.reader);
        VarScope varScope = parseVarScope(self.reader);
        string varName = self.reader.readStringCpRef();
        var decl = self.getDecl(varScope, varName, kind);
        return {typeValue : decl.typeValue, variableDcl : decl};
    }

    public function parseBBRef() returns BasicBlock {
        return { id: { value: self.reader.readStringCpRef() } };
    }

    public function parseBinaryOpInstruction(int kindTag, DiagnosticPos pos) returns BinaryOp {
        BinaryOpInstructionKind kind = BINARY_ADD;
        if (kindTag == INS_ADD){
            kind = BINARY_ADD;
        } else if (kindTag == INS_SUB){
            kind = BINARY_SUB;
        } else if (kindTag == INS_MUL){
            kind = BINARY_MUL;
        } else if (kindTag == INS_DIV){
            kind = BINARY_DIV;
        } else if (kindTag == INS_MOD){
            kind = BINARY_MOD;
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
        } else if (kindTag == INS_REF_EQUAL){
            kind = BINARY_REF_EQUAL;
        } else if (kindTag == INS_REF_NOT_EQUAL){
            kind = BINARY_REF_NOT_EQUAL;
        } else if (kindTag == INS_CLOSED_RANGE) {
            kind = BINARY_CLOSED_RANGE;
        } else if (kindTag == INS_HALF_OPEN_RANGE) {
            kind = BINARY_HALF_OPEN_RANGE;
        } else if (kindTag == INS_ANNOT_ACCESS){
            kind = BINARY_ANNOT_ACCESS;
        } else if (kindTag == INS_BITWISE_AND) {
            kind = BINARY_BITWISE_AND;
        } else if (kindTag == INS_BITWISE_OR) {
            kind = BINARY_BITWISE_OR;
        } else if (kindTag == INS_BITWISE_XOR) {
            kind = BINARY_BITWISE_XOR;
        } else if (kindTag == INS_BITWISE_LEFT_SHIFT) {
            kind = BINARY_BITWISE_LEFT_SHIFT;
        } else if (kindTag == INS_BITWISE_RIGHT_SHIFT) {
            kind = BINARY_BITWISE_RIGHT_SHIFT;
        } else if (kindTag == INS_BITWISE_UNSIGNED_RIGHT_SHIFT) {
            kind = BINARY_BITWISE_UNSIGNED_RIGHT_SHIFT;
        } else {
            error err = error("instrucion kind " + kindTag + " not impl.");
            panic err;
        }

        var rhsOp1 = self.parseVarRef();
        var rhsOp2 = self.parseVarRef();
        var lhsOp = self.parseVarRef();
        BinaryOp binaryOp = {pos:pos, kind:kind, lhsOp:lhsOp, rhsOp1:rhsOp1, rhsOp2:rhsOp2};
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

    private function getDecl(VarScope varScope, string varName, VarKind kind) returns VariableDcl {
        if (varScope == VAR_SCOPE_GLOBAL) {
            if (kind == VAR_KIND_CONSTANT) {
                var bType = self.reader.readTypeCpRef();
                ModuleID pkgId = self.reader.readModuleIDCpRef();
                VariableDcl varDecl = { kind : kind, 
                                        varScope : varScope, 
                                        name : {value : varName},
                                        typeValue : bType,
                                        moduleId : pkgId
                                    };
                return varDecl;
            }

            var possibleDcl = self.globalVarMap[varName];
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

        var possibleDcl = self.localVarMap[varName];
        if (possibleDcl is VariableDcl) {
            return possibleDcl;
        } else {
            error err = error("local var missing " + varName);
            panic err;
        }
    }
};


public function parseVariableDcl(BirChannelReader reader) returns VariableDcl {
    VarKind kind = parseVarKind(reader);
    var typeValue = reader.readTypeCpRef();
    var name = reader.readStringCpRef();
    VariableDcl dcl = {
        typeValue: typeValue,
        name: { value: name },
            kind: kind
        };
    return dcl;
}
