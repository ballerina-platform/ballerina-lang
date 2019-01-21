public type PackageParser object {
    BirChannelReader reader;

    public function __init(BirChannelReader reader) {
        self.reader = reader;
    }

    public function parseVariableDcl() returns VariableDcl {
        var kind = self.parseVarKind();
        VariableDcl dcl = {
            typeValue: self.reader.readBType(),
            name: { value: self.reader.readStringCpRef() },
            kind:kind
        };
        return dcl;
    }

    public function parseFunction() returns Function {
        var name = self.reader.readStringCpRef();
        var isDeclaration = self.reader.readBoolean();
        var visibility = self.parseVisibility();
        var sig = self.reader.readStringCpRef();
        var argsCount = self.reader.readInt32();
        var numLocalVars = self.reader.readInt32();

        VariableDcl?[] dcls = [];
        map<VariableDcl> localVarMap = {};
        int i = 0;
        while (i < numLocalVars) {
            var dcl = self.parseVariableDcl();
            dcls[i] = dcl;
            localVarMap[dcl.name.value] = dcl;
            i += 1;
        }
        FuncBodyParser bodyParser = new(self.reader, localVarMap);

        BasicBlock?[] basicBlocks = [];
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
            typeValue: self.parseSig(sig)
        };
    }

    public function parsePackage() returns Package {
        var pkgIdCp = self.reader.readInt32();
        var numFuncs = self.reader.readInt32();
        Function?[] funcs = [];
        int i = 0;
        while (i < numFuncs) {
            funcs[i] = self.parseFunction();
            i += 1;
        }
        return { functions:funcs };
    }


    public function parseVisibility() returns Visibility {
        int b = self.reader.readInt8();
        if (b == 0){
            return "PACKAGE_PRIVATE";
        } else if (b == 1){
            return "PRIVATE";
        } else if (b == 2){
            return "PUBLIC";
        }
        error err = error("unknown variable visiblity tag " + b);
        panic err;
    }

    public function parseVarKind() returns VarKind {
        int b = self.reader.readInt8();
        if (b == 1){
            return "LOCAL";
        } else if (b == 2){
            return "ARG";
        } else if (b == 3){
            return "TEMP";
        } else if (b == 4){
            return "RETURN";
        }
        error err = error("unknown var kind tag " + b);
        panic err;
    }

    public function parseSig(string sig) returns BInvokableType {
        BType returnType = "int";
        //TODO: add boolean
        if (sig.lastIndexOf("(N)") == (sig.length() - 3)){
            returnType = "()";
        }
        return {
            retType:returnType
        };
    }

};

