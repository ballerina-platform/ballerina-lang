import ballerina/io;

import wso2/nballerina.bir;
import wso2/nballerina.types as t;
import wso2/nballerina.front.syntax as s;
import wso2/nballerina.err;

type ModuleTable table<s:ModuleLevelDefn> key(name);

class Module {
    *bir:Module;
    final bir:ModuleId id;
    final map<bir:ModuleId> imports;
    final ModuleTable defns;
    final t:TypeCheckContext tc;
    final s:FunctionDefn[] functionDefnSource = [];
    final readonly & bir:FunctionDefn[] functionDefns;

    function init(bir:ModuleId id, map<bir:ModuleId> imports, ModuleTable defns, t:TypeCheckContext tc) {
        self.id = id;
        self.imports = imports;
        self.defns = defns;
        self.tc = tc;
        final bir:FunctionDefn[] functionDefns = [];
        foreach var defn in defns {
            if defn is s:FunctionDefn {
                self.functionDefnSource.push(defn);
                functionDefns.push({
                    symbol: <bir:InternalSymbol>{ identifier: defn.name, isPublic: defn.vis == "public" },
                    // casting away nil here, because it was filled in by `resolveTypes`
                    signature: <bir:FunctionSignature>defn.signature,
                    position: defn.pos
                });
            }
        }
        self.functionDefns = functionDefns.cloneReadOnly();
    }

    public function getId() returns bir:ModuleId => self.id;

    public function getTypeCheckContext() returns t:TypeCheckContext => self.tc;

    public function generateFunctionCode(int i) returns bir:FunctionCode|err:Semantic|err:Unimplemented {
        s:FunctionDefn ast = self.functionDefnSource[i];
        return codeGenFunction(self, ast.file, ast.name, self.functionDefns[i].signature, ast.paramNames, ast.body);
    }
   
    public function getFunctionDefns() returns readonly & bir:FunctionDefn[] {
        return self.functionDefns;
    }

    public function getPrefixForModuleId(bir:ModuleId id) returns string? {
        foreach var [prefix, moduleId] in self.imports.entries() {
            if moduleId == id {
                return  prefix;
            }
        }
        return ();
    }

}

public function loadModule(t:Env env, string filename, bir:ModuleId id) returns bir:Module|err:Any|io:Error {
    string[] lines = check io:fileReadLines(filename);
    s:ModulePart part = check s:parseModulePart(lines, filename);
    ModuleTable mod = table [];
    check addModulePart(mod, part);
    check resolveTypes(env, mod);
    // XXX Should have an option that controls whether we perform this check
    check validEntryPoint(mod);
    return new Module(id, imports(part), mod, t:typeCheckContext(env));
}

function imports(s:ModulePart part) returns map<bir:ModuleId> {
    s:ImportDecl? decl = part.importDecl;
    if decl == () {
        return {};
    }
    else {
        return { [decl.module]: { organization: decl.org, names: [decl.module]} };
    }
}

function validEntryPoint(ModuleTable mod) returns err:Any? {
    s:ModuleLevelDefn? defn = mod["main"];
    if defn is s:FunctionDefn {
        if defn.vis != "public" {
            return err:semantic(`${"main"} is not public`, s:defnLocation(defn));
        }
        if defn.paramNames.length() > 0 {
            return err:unimplemented(`parameters for ${"main"} not yet implemented`, s:defnLocation(defn));
        }
        if (<bir:FunctionSignature>defn.signature).returnType !== t:NIL {
            return err:semantic(`return type for ${"main"} must be subtype of ${"error?"}`, s:defnLocation(defn));
        }
    }
}

function addModulePart(ModuleTable mod, s:ModulePart part) returns err:Semantic? {
    foreach s:ModuleLevelDefn defn in part.defns {
        if mod.hasKey(defn.name) {
            return err:semantic(`duplicate definition if ${defn.name}`);
        }
        mod.add(defn); 
    }
}

// This is old interface for showTypes
public function typesFromString(string[] lines, string filename) returns [t:Env, map<t:SemType>]|err:Any {
    s:ModulePart part = check s:parseModulePart(lines, filename);
    ModuleTable mod = table [];
    check addModulePart(mod, part);
    t:Env env = new;
    check resolveTypes(env, mod);
    return [env, createTypeMap(mod)];
}

