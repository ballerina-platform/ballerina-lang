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

public type PackageParser object {
    BirChannelReader reader;
    boolean addInterimBB = true;
    boolean symbolsOnly;

    public function __init(BirChannelReader reader, boolean symbolsOnly) {
        self.reader = reader;
        self.symbolsOnly = symbolsOnly;
    }

    public function parseVariableDcl() returns VariableDcl {
        VarKind kind = parseVarKind(self.reader);
        var typeValue = self.reader.readTypeCpRef();
        var name = self.reader.readStringCpRef();

        VariableDcl dcl = {
            typeValue: typeValue,
            name: { value: name },
            kind: kind
        };

        return dcl;
    }

    function skipAnnotations() {
        var numAnnotations = self.reader.readInt32();
        int i = 0;
        while (i < numAnnotations) {
            self.skipAnnotation();
            i += 1;
        }
    }

    public function skipAnnotation() {
        _ = self.reader.readInt32();
        _ = self.reader.readInt32();

        int attachPointCount = self.reader.readInt32();
        int i = 0;
        while (i < attachPointCount) {
            _ = self.reader.readInt32();
            _ = self.reader.readBoolean();
            i += 1;
        }

        _ = self.reader.readTypeCpRef();
    }

    function parseConstants(GlobalVariableDcl?[] globalVars) {
        int numConstants = self.reader.readInt32();
        int i = 0;
        while i < numConstants {
            string name = self.reader.readStringCpRef();
            int flags = self.reader.readInt32();

            skipMarkDownDocAttachement(self.reader); 

            var typeValue = self.reader.readTypeCpRef();

            int constValueLength = self.reader.readInt64();
            _ = self.reader.readByteArray(<@untainted> constValueLength);

            GlobalVariableDcl dcl = { kind:VAR_KIND_CONSTANT, 
                                      name:{value:name}, 
                                      typeValue:typeValue, 
                                      flags:flags
                                    };
            globalVars[i] = dcl;
            i = i + 1;
        }
    }

    public function parseFunctionParam() returns FunctionParam {
        VarKind kind = parseVarKind(self.reader);
        var typeValue = self.reader.readTypeCpRef();
        var name = self.reader.readStringCpRef();
        VariableDclMeta meta = {};
        if (kind is ArgVarKind) {
            meta.name = self.reader.readStringCpRef();
        }
        var hasDefaultExpr = self.reader.readBoolean();
        FunctionParam dcl = {
            typeValue: typeValue,
            name: { value: name },
            kind: kind,
            hasDefaultExpr: hasDefaultExpr,
            meta: meta
        };
        return dcl;
    }

    function parseFunctions(TypeDef?[] typeDefs) returns Function?[] {
        var numFuncs = self.reader.readInt32();
        Function?[] funcs = [];
        int i = 0;
        while (i < numFuncs) {
            funcs[i] = self.parseFunction(typeDefs);
            i += 1;
        }
        // TODO: dhananjaya please remove
        return <@untainted> funcs;
    }

    private function parseInvokableType() returns BInvokableType {
        var functionType = self.reader.readTypeCpRef();
        if (functionType is BInvokableType) {
            return functionType;
        }
        error err = error("Illegal function signature type");
        panic err;
    }

    public function parseFunction(TypeDef?[] typeDefs) returns @untainted Function {
        map<VariableDcl> localVarMap = {};
        DiagnosticPos pos = parseDiagnosticPos(self.reader);
        var name = self.reader.readStringCpRef();
        var workerName = self.reader.readStringCpRef();
        int flags = self.reader.readInt32();
        var sig = self.parseInvokableType();

        // Read annotation Attachments
        AnnotationAttachment?[] annotAttachments = parseAnnotAttachments(self.reader);

        // Read and ignore parameter details, not used in jvm gen
        self.readAndIgnoreParamDetails();

        // Need to track a rest param Exist
        boolean restParamExist = self.reader.readBoolean();
        if (restParamExist) {
            _ = self.reader.readInt32();
        }

        VariableDcl? receiver = ();
        boolean hasReceiver = self.reader.readBoolean();
        if (hasReceiver) {
            receiver = self.parseVariableDcl();
        }

        int taintLength = self.reader.readInt64();
        _ = self.reader.readByteArray(<@untainted> taintLength); // read and ignore taint table
        
        skipMarkDownDocAttachement(self.reader); 

        var bodyLength = self.reader.readInt64(); // read and ignore function body length
        if (self.symbolsOnly) {
            _ = self.reader.readByteArray(<@untainted> bodyLength);
            return <@untainted Function> {
                pos: pos,
                name: { value: name },
                workerName: { value: workerName },
                flags: flags,
                localVars: [],
                basicBlocks: [],
                params: [],
                paramDefaultBBs: [],
                errorEntries: [],
                argsCount: 0,
                typeValue: sig,
                workerChannels: [],
                receiver : receiver,
                restParamExist : restParamExist,
                annotAttachments: annotAttachments
            };
        }

        var argsCount = self.reader.readInt32();

        VariableDcl?[] dcls = [];
        var haveReturn = self.reader.readBoolean();
        if (haveReturn) {
            var dcl = self.parseVariableDcl();
            dcls[dcls.length()] = dcl;
            localVarMap[dcl.name.value] = dcl;
        }

        var numParameters = self.reader.readInt32();
        int count = 0;
        int numDefaultParams = 0;
        FunctionParam?[] params = [];
        while (count < numParameters) {
            FunctionParam dcl = self.parseFunctionParam();
            params[count] = dcl;
            if (dcl.hasDefaultExpr) {
                numDefaultParams = numDefaultParams + 1;
            }
            dcls[dcls.length()] = dcl;
            localVarMap[dcl.name.value] = dcl;
            count += 1;
        }

        count = 0;
        var numLocalVars = self.reader.readInt32();
        while (count < numLocalVars) {
            var dcl = self.parseVariableDcl();
            if (dcl.kind is LocalVarKind || dcl.kind is ArgVarKind) {
                dcl.meta.name = self.reader.readStringCpRef();
                // local variables have visible range information as well
                if (dcl.kind is LocalVarKind) {
                    var meta = dcl.meta;
                    meta.endBBID = self.reader.readStringCpRef();
                    meta.startBBID = self.reader.readStringCpRef();
                    meta.insOffset = self.reader.readInt32();
                }
            }
            dcls[dcls.length()] = dcl;
            localVarMap[dcl.name.value] = dcl;
            count += 1;
        }

        FuncBodyParser bodyParser = new(self.reader, localVarMap, typeDefs, receiver);
        count = 0;
        BasicBlock?[][] paramDefaultBBs = [];
        while (count < numDefaultParams) {
            paramDefaultBBs[count] = self.getBasicBlocks(bodyParser);
            count += 1;
        }

        BasicBlock?[] basicBlocks = self.getBasicBlocks(bodyParser);
        ErrorEntry?[] errorEntries = self.getErrorEntries(bodyParser);
        ChannelDetail[] workerChannels = getWorkerChannels(self.reader);

        return <@untainted Function> {
            pos: pos,
            name: { value: name },
            workerName: { value: workerName },
            flags: flags,
            localVars: dcls,
            basicBlocks: basicBlocks,
            params: params,
            paramDefaultBBs: paramDefaultBBs,
            errorEntries:errorEntries,
            argsCount: argsCount,
            typeValue: sig,
            workerChannels:workerChannels,
            receiver : receiver,
            restParamExist : restParamExist,
            annotAttachments: annotAttachments
        };
    }

    private function readAndIgnoreParamDetails() {
        int requiredParamCount = self.reader.readInt32();
        int i = 0;
        while (i < requiredParamCount) {
            // ignore name
            _ = self.reader.readInt32();
            // ignore flags
            _ = self.reader.readInt32();
            i += 1;
        }
    }

    public function parsePackage() returns Package {
        ModuleID pkgId = self.reader.readModuleIDCpRef();
        ImportModule[] importModules = self.parseImportMods();

        GlobalVariableDcl?[] globalVars = [];
        self.parseConstants(globalVars);
        TypeDef?[] typeDefs = self.parseTypeDefs();
        self.parseGlobalVars(globalVars);

        // Parse type def bodies after parsing global vars.
        // This is done avoid cyclic dependencies.
        self.parseTypeDefBodies(typeDefs);

        Function?[] funcs = self.parseFunctions(typeDefs);

        self.skipAnnotations();

        return <@untainted Package> { importModules : importModules,
                    typeDefs : typeDefs, 
                    globalVars : globalVars, 
                    functions : funcs,
                    name : { value: pkgId.name }, 
                    org : { value: pkgId.org }, 
                    versionValue : { value: pkgId.modVersion } };
    }

    function getBasicBlocks(FuncBodyParser bodyParser) returns BasicBlock?[] {
        BasicBlock?[] basicBlocks = [];
        var numBB = self.reader.readInt32();
        int i = 0;
        int j = 0;
        while (i < numBB) {
            BasicBlock[] blocks = bodyParser.parseBB(self.addInterimBB);
            basicBlocks[j] = blocks[0];
            j += 1;
            if (self.addInterimBB) {
                basicBlocks[j] = blocks[1];
                j += 1;
            }
            i += 1;
        }
        return basicBlocks;
    }

    function getErrorEntries(FuncBodyParser bodyParser) returns ErrorEntry?[] {
        ErrorEntry?[] errorEntries = [];
        var numEE = self.reader.readInt32();
        int i = 0;
        int j = 0;
        while (i < numEE) {
            errorEntries[j] = bodyParser.parseEE();
            
            if (self.addInterimBB) {
                ErrorEntry? interimEntry = errorEntries[j].clone();
                j += 1;
                if (interimEntry is ErrorEntry) {
                    interimEntry.trapBB.id.value = interimEntry.trapBB.id.value + "interim";
                    errorEntries[j] = interimEntry;                    
                }
            }
            j += 1;
            i += 1;   
        }
        return errorEntries;
    }

    function parseImportMods() returns ImportModule[] {
        int numImportMods = self.reader.readInt32();
        ImportModule[] importModules = [];
        foreach var i in 0..<numImportMods {
            string modOrg = self.reader.readStringCpRef();
            string modName = self.reader.readStringCpRef();
            string modVersion = self.reader.readStringCpRef();
            importModules[i] = { modOrg: { value: modOrg }, modName: { value: modName },
                modVersion: { value: modVersion } };
        }
        return importModules;
    }

    function parseTypeDefs() returns TypeDef?[] {
        int numTypeDefs = self.reader.readInt32();
        TypeDef?[] typeDefs = [];
        int i = 0;
        while i < numTypeDefs {
            typeDefs[i] = self.parseTypeDef();
            i = i + 1;
        }

        return typeDefs;
    }

    function parseTypeDefBodies(TypeDef?[] typeDefs) {
        int numTypeDefs = typeDefs.length();
        foreach var typeDef in typeDefs {
            TypeDef tDef = getTypeDef(typeDef);
            BType typeValue = tDef.typeValue;
            if (typeValue is BObjectType || typeValue is BRecordType || typeValue is BServiceType) {
                tDef.attachedFuncs = self.parseFunctions(typeDefs);
                tDef.typeRefs = self.parseReferencedTypes();
            }
        }
    }

    function parseReferencedTypes() returns BType?[] {
        var numTypes = self.reader.readInt32();
        BType?[] typeRefs = [];
        int i = 0;
        while (i < numTypes) {
            typeRefs[i] = self.reader.readTypeCpRef();
            i += 1;
        }

        return <@untainted> typeRefs;
    }

    function parseTypeDef() returns TypeDef {
        DiagnosticPos pos = parseDiagnosticPos(self.reader);
        string name = self.reader.readStringCpRef();
        int flags = self.reader.readInt32();
        int isLabel = self.reader.readInt8();

        skipMarkDownDocAttachement(self.reader); 

        var bType = self.reader.readTypeCpRef();
        return { pos:pos, name: { value: name }, flags: flags, typeValue: bType, attachedFuncs: (), typeRefs : [] };
    }

    function parseGlobalVars(GlobalVariableDcl?[] globalVars) {       
        int numGlobalVars = self.reader.readInt32();
        int startIndex = globalVars.length();   
        int i = 0;
        while i < numGlobalVars {
            var kind = parseVarKind(self.reader);
            string name = self.reader.readStringCpRef();
            int flags = self.reader.readInt32();
            skipMarkDownDocAttachement(self.reader); 
            var typeValue = self.reader.readTypeCpRef();
            GlobalVariableDcl dcl = {kind:kind, name:{value:name}, typeValue:typeValue, flags:flags};
            globalVars[startIndex + i] = dcl;
            i = i + 1;
        }
    }

    public function parseSig(string sig) returns BInvokableType {
        BType returnType = "int";
        //TODO: add boolean
        if (internal:lastIndexOf(sig, "(N)") == (sig.length() - 3)) {
            returnType = "()";
        }
        return {
            retType: returnType
        };
    }
};

function skipMarkDownDocAttachement(BirChannelReader reader) {
    int docLength = reader.readInt32();
    _ = reader.readByteArray(<@untainted> docLength);
}

function parseLiteralValue(BirChannelReader reader, BType bType) returns anydata {
    anydata value;
    if (bType is BTypeByte) {
        value = reader.readByteCpRef();
    } else if (bType is BTypeInt) {
        value = reader.readIntCpRef();
    } else if (bType is BTypeString) {
        value = reader.readStringCpRef();
    } else if (bType is BTypeDecimal) {
        Decimal d = {value : reader.readStringCpRef()};
        value = d;
    } else if (bType is BTypeBoolean) {
        value = reader.readBoolean();
    } else if (bType is BTypeFloat) {
        value = reader.readFloatCpRef();
    } else {
        record{| anydata|error...; |} detailMap = { "type" : bType };
        error err = error("unsupported literal value type in annotation attachment value", err = detailMap);
        panic err;
    }
    return value;
}

public function parseVarKind(BirChannelReader reader) returns VarKind {
    int b = reader.readInt8();
    if (b == 1) {
        LocalVarKind local = VAR_KIND_LOCAL;
        return local;
    } else if (b == 2) {
        ArgVarKind arg = VAR_KIND_ARG;
        return arg;
    } else if (b == 3) {
        TempVarKind temp = VAR_KIND_TEMP;
        return temp;
    } else if (b == 4) {
        ReturnVarKind ret = VAR_KIND_RETURN;
        return ret;
    } else if (b == 5) {
        GlobalVarKind ret = VAR_KIND_GLOBAL;
        return ret;
    }  else if (b == 6) {
        SelfVarKind ret = VAR_KIND_SELF;
        return ret;
    }  else if (b == 7) {
        ConstantVarKind ret = VAR_KIND_CONSTANT;
        return ret;
    } 

    error err = error("unknown var kind tag " + b.toString());
    panic err;
}

public function parseVarScope(BirChannelReader reader) returns VarScope {
    int b = reader.readInt8();
    if (b == 1) {
        VarScope local = VAR_SCOPE_FUNCTION;
        return local;
    } else if (b == 2) {
        VarScope glob = VAR_SCOPE_GLOBAL;
        return glob;
    }
    error err = error("unknown var scope tag " + b.toString());
    panic err;
}

public function parseDiagnosticPos(BirChannelReader reader) returns DiagnosticPos {
    int sLine = reader.readInt32();
    int eLine = reader.readInt32();
    int sCol = reader.readInt32();
    int eCol = reader.readInt32();
    string sourceFileName = reader.readStringCpRef();
    return { sLine:sLine, eLine:eLine, sCol:sCol, eCol:eCol, sourceFileName:sourceFileName };
}

function getWorkerChannels(BirChannelReader reader) returns ChannelDetail[] {
    ChannelDetail[] channelDetails = [];
    var count = reader.readInt32();
    int i = 0;
    while (i < count) {
        string name = reader.readStringCpRef();
        boolean onSameStrand = reader.readBoolean();
        boolean isSend = reader.readBoolean();
        channelDetails[i] = { name: { value:name }, onSameStrand:onSameStrand, isSend:isSend };
        i += 1;
    }
    return channelDetails;
}

function parseAnnotAttachments(BirChannelReader reader) returns AnnotationAttachment?[] {
    AnnotationAttachment?[] annotAttachments = [];
    int annotNoBytes_ignored = reader.readInt64();
    int noOfAnnotAttachments = reader.readInt32();
    if (noOfAnnotAttachments == 0) {
        return annotAttachments;
    }

    foreach var i in 0..<noOfAnnotAttachments {
        annotAttachments[annotAttachments.length()] = parseAnnotAttachment(reader);
    }
    //io:println(annotAttachments);

    return annotAttachments;
}

function parseAnnotAttachment(BirChannelReader reader) returns AnnotationAttachment {
    // Read ModuleID
    ModuleID modId = reader.readModuleIDCpRef();
    // Read DiagnosticPos
    DiagnosticPos pos = parseDiagnosticPos(reader);
    // Read AnnotTagRef
    string annotTagRef = reader.readStringCpRef();
    // Read AnnotationValue values
    AnnotationValue?[] annotValues = [];
    int noOfAnnotValue = reader.readInt32();
    foreach var i in 0..<noOfAnnotValue {
        annotValues[annotValues.length()] = parseAnnotAttachValue(reader);
    }

    return {
        moduleId: modId,
        pos: pos,
        annotTagRef: {value:annotTagRef},
        annotValues:annotValues
    };
}

function parseAnnotAttachValue(BirChannelReader reader) returns AnnotationValue {
    var bType = reader.readTypeCpRef();
    if bType is BArrayType {
        return parseAnnotArrayValue(reader);
    } else if bType is BMapType || bType is BRecordType {
        return parseAnnotRecordValue(reader);
    } else {
        // This is a value type
        return parseAnnotLiteralValue(reader, bType);
    }
}

function parseAnnotLiteralValue(BirChannelReader reader, BType bType) returns AnnotationLiteralValue {
    var value = parseLiteralValue(reader, bType);
    return {
        literalType: bType,
        literalValue: value
    };
}

function parseAnnotRecordValue(BirChannelReader reader) returns AnnotationRecordValue {
    map<AnnotationValue> annotValueMap = {};
    var noOfAnnotValueEntries = reader.readInt32();
    foreach var i in 0..<noOfAnnotValueEntries {
        var key = reader.readStringCpRef();
        var annotValue = parseAnnotAttachValue(reader);
        annotValueMap[key] = annotValue;
    }
    return {
        annotValueMap: annotValueMap
    };
}

function parseAnnotArrayValue(BirChannelReader reader) returns AnnotationArrayValue {
    AnnotationValue?[]  annotValueArray = [];
    var noOfAnnotValueEntries = reader.readInt32();
    foreach var i in 0..<noOfAnnotValueEntries {
        var annotValue = parseAnnotAttachValue(reader);
        annotValueArray[annotValueArray.length()] = annotValue;
    }
    return {
        annotValueArray: annotValueArray
    };
}
