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

public type PackageParser object {
    BirChannelReader reader;
    TypeParser typeParser;
    map<VariableDcl> globalVarMap;

    public function __init(BirChannelReader reader, TypeParser typeParser) {
        self.reader = reader;
        self.typeParser = typeParser;
        self.globalVarMap = {};
    }

    public function parseVariableDcl() returns VariableDcl {
        VarKind kind = parseVarKind(self.reader);
        var typeValue = self.typeParser.parseType();
        var name = self.reader.readStringCpRef();
        VariableDcl dcl = {
            typeValue: typeValue,
            name: { value: name },
            kind: kind
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
        return funcs;
    }

    public function parseFunction(TypeDef?[] typeDefs) returns Function {
        map<VariableDcl> localVarMap = {};
        FuncBodyParser bodyParser = new(self.reader, self.typeParser, self.globalVarMap, localVarMap, typeDefs);
        DiagnosticPos pos = parseDiagnosticPos(self.reader);
        var name = self.reader.readStringCpRef();
        var isDeclaration = self.reader.readBoolean();
        var isInterface = self.reader.readBoolean();
        var visibility = parseVisibility(self.reader);
        var typeTag = self.reader.readInt8();
        if (typeTag != self.typeParser.TYPE_TAG_INVOKABLE) {
            error err = error("Illegal function signature type tag" + typeTag);
            panic err;
        }
        var sig = self.typeParser.parseInvokableType();
        var argsCount = self.reader.readInt32();
        var numLocalVars = self.reader.readInt32();

        VariableDcl?[] dcls = [];
        int i = 0;
        while (i < numLocalVars) {
            var dcl = self.parseVariableDcl();
            dcls[i] = dcl;
            localVarMap[dcl.name.value] = dcl;
            i += 1;
        }
        BasicBlock?[] basicBlocks = self.getBasicBlocks(bodyParser);
        ErrorEntry?[] errorEntries = self.getErrorEntries(bodyParser);
        return {
            pos: pos,
            name: { value: name },
            isDeclaration: isDeclaration,
            isInterface:isInterface,
            visibility: visibility,
            localVars: dcls,
            basicBlocks: basicBlocks,
            errorEntries:errorEntries,
            argsCount: argsCount,
            typeValue: sig
        };
    }

    public function parsePackage() returns Package {
        ModuleID pkgId = self.reader.readModuleIDCpRef();
        ImportModule[] importModules = self.parseImportMods();
        TypeDef?[] typeDefs = self.parseTypeDefs();
        GlobalVariableDcl?[] globalVars = self.parseGlobalVars();

        // Parse type def bodies after parsing global vars.
        // This is done avoid cyclic dependencies.
        self.parseTypeDefBodies(typeDefs);

        Function?[] funcs = self.parseFunctions(typeDefs);

        return { importModules : importModules,
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
        while (i < numBB) {
            basicBlocks[i] = bodyParser.parseBB();
            i += 1;
        }

        return basicBlocks;
    }

    function getErrorEntries(FuncBodyParser bodyParser) returns ErrorEntry?[] {
        ErrorEntry?[] errorEntries = [];
        var numEE = self.reader.readInt32();
        int i = 0;
        while (i < numEE) {
            errorEntries[i] = bodyParser.parseEE();
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
            if (typeValue is BObjectType || typeValue is BRecordType) {
                tDef.attachedFuncs = self.parseFunctions(typeDefs);
            }
        }
    }

    function parseTypeDef() returns TypeDef {
        DiagnosticPos pos = parseDiagnosticPos(self.reader);
        string name = self.reader.readStringCpRef();
        Visibility visibility = parseVisibility(self.reader);
        var bType = self.typeParser.parseType();
        return { pos:pos, name: { value: name }, visibility: visibility, typeValue: bType, attachedFuncs: () };
    }

    function parseGlobalVars() returns GlobalVariableDcl?[] {       
        GlobalVariableDcl?[] globalVars = []; 
        int numGlobalVars = self.reader.readInt32();        
        int i = 0;
        while i < numGlobalVars {
            var kind = parseVarKind(self.reader);
            string name = self.reader.readStringCpRef();
            Visibility visibility = parseVisibility(self.reader);
            var typeValue = self.typeParser.parseType();
            GlobalVariableDcl dcl = {kind:kind, name:{value:name}, typeValue:typeValue, visibility:visibility};
            globalVars[i] = dcl;
            self.globalVarMap[name] = dcl;
            i = i + 1;
        }
        return globalVars;
    }

    public function parseSig(string sig) returns BInvokableType {
        BType returnType = "int";
        //TODO: add boolean
        if (sig.lastIndexOf("(N)") == (sig.length() - 3)) {
            returnType = "()";
        }
        return {
            retType: returnType
        };
    }

};

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
    } 
    error err = error("unknown var kind tag " + b);
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
    error err = error("unknown var scope tag " + b);
    panic err;
}

public function parseVisibility(BirChannelReader reader) returns Visibility {
    int b = reader.readInt8();
    if (b == 0) {
        return "PACKAGE_PRIVATE";
    } else if (b == 1) {
        return "PRIVATE";
    } else if (b == 2) {
        return "PUBLIC";
    }
    error err = error("unknown variable visiblity tag " + b);
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

