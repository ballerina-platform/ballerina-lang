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

    public function parseFunction() returns Function {
        var name = self.reader.readStringCpRef();
        var isDeclaration = self.reader.readBoolean();
        var visibility = parseVisibility(self.reader);
        var typeTag = self.reader.readInt8();
        if (typeTag != self.typeParser.TYPE_TAG_INVOKABLE) {
            error err = error("Illegal function signature type tag" + typeTag);
            panic err;
        }
        var sig = self.typeParser.parseInvokableType();
        var argsCount = self.reader.readInt32();
        var numLocalVars = self.reader.readInt32();

        VariableDcl[] dcls = [];
        map<VariableDcl> localVarMap = {
        
        };
        int i = 0;
        while (i < numLocalVars) {
            var dcl = self.parseVariableDcl();
            dcls[i] = dcl;
            localVarMap[dcl.name.value] = dcl;
            i += 1;
        }
        FuncBodyParser bodyParser = new(self.reader, self.typeParser, self.globalVarMap, localVarMap);

        BasicBlock[] basicBlocks = [];
        var numBB = self.reader.readInt32();
        i = 0;
        while (i < numBB) {
            basicBlocks[i] = bodyParser.parseBB();
            i += 1;
        }

        return {
            name: { value: name },
            isDeclaration: isDeclaration,
            visibility: visibility,
            localVars: dcls,
            basicBlocks: basicBlocks,
            argsCount: argsCount,
            typeValue: sig
        };
    }

    public function parsePackage() returns Package {
        ModuleID pkgId = self.reader.readModuleIDCpRef();
        ImportModule[] importModules = self.parseImportMods();
        TypeDef[] typeDefs = self.parseTypeDefs();
        GlobalVariableDcl[] globalVars = self.parseGlobalVars();
        var numFuncs = self.reader.readInt32();
        Function[] funcs = [];
        int i = 0;
        while (i < numFuncs) {
            funcs[i] = self.parseFunction();
            i += 1;
        }

//       BirEmitter emitter = new({ importModules: importModules, typeDefs: typeDefs, globalVars:globalVars, functions: funcs,
//                   name: {value: pkgId.name}, org: {value: pkgId.org}, versionValue: {value: pkgId.modVersion}});
//       emitter.emitPackage();

        return { importModules: importModules, typeDefs: typeDefs, globalVars:globalVars, functions: funcs,
                name: {value: pkgId.name}, org: {value: pkgId.org}, versionValue: {value: pkgId.modVersion}};
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

    function parseTypeDefs() returns TypeDef[] {
        int numTypeDefs = self.reader.readInt32();
        TypeDef[] typeDefs = [];
        int i = 0;
        while i < numTypeDefs {
            typeDefs[i] = self.parseTypeDef();
            i = i + 1;
        }
        return typeDefs;
    }

    function parseTypeDef() returns TypeDef {
        string name = self.reader.readStringCpRef();
        Visibility visibility = parseVisibility(self.reader);
        return { name:{ value: name}, visibility: visibility, typeValue: self.typeParser.parseType()};
    }

    function parseGlobalVars() returns GlobalVariableDcl[] {       
        GlobalVariableDcl[] globalVars = []; 
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
        LocalVarKind local = "LOCAL";
        return local;
    } else if (b == 2) {
        ArgVarKind arg = "ARG";
        return arg;
    } else if (b == 3) {
        TempVarKind temp = "TEMP";
        return temp;
    } else if (b == 4) {
        ReturnVarKind ret = "RETURN";
        return ret;
    } else if (b == 5) {
        GlobalVarKind ret = "GLOBAL";
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

