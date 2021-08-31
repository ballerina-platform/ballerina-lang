// Convert from json into a SemType
// Format is defined in schema.bal
import wso2/nballerina.types as t;


public type Path int[];

public type ParseDetail record {
    Path path;
};

public type ParseError error<ParseDetail>;

type Binding NameBinding|DefBinding;

type NameBinding record {|
    string name;
    json desc;
    Path path;
    Binding? next;
|};

type DefBinding record {|
   json desc;
   t:Definition def;
   Binding? next;
|};

public function parse(t:Env env, json j) returns t:SemType|ParseError {
    return parseType(env, (), j, []);
} 

function parseType(t:Env env, Binding? b, json j, Path path) returns t:SemType|ParseError {
    match j {
        Nil => { return t:NIL; }
        Boolean => { return t:BOOLEAN; }
        Int => { return t:INT; }
        Byte => { return t:BYTE; }
        "int8" => { return t:intWidthSigned(8); }
        "int16" => { return t:intWidthSigned(16); }
        "int32" => { return t:intWidthSigned(32); }
        "uint8" => { return t:intWidthUnsigned(8); }
        "uint16" => { return t:intWidthUnsigned(16); }
        "uint32" => { return t:intWidthUnsigned(32); }
        Float => { return t:FLOAT; }
        Decimal => { return t:DECIMAL; }
        String => { return t:STRING; }
        "error" => { return t:ERROR; }
        Typedesc => { return t:TYPEDESC; }
        Handle => { return t:HANDLE; }
        Xml => { return t:XML; } 
        Json => { return t:createJson(env); }
        Any => { return t:ANY; }
        Never => { return t:NEVER; }
        ReadOnly => { return t:READONLY; }
        true => { return t:booleanConst(true); }
        false => { return t:booleanConst(false); }
        // Should be able to use match patterns here
        // but there's a compiler bug #29041
        var js if js is json[] => {
            if js.length() == 0 {
                path.push(0);
                return parseError("empty array", path);
            }
            else {
                json k = js[0];
                if !(k is string) {
                    return parseError("expected array to begin with string", path, 0);
                }
                else {
                    return parseCompoundType(env, b, k, js, path);
                }
            }
           
        }
        var s if s is string => {
            return parseError("unrecognized keyword '" + s + "'", path);
        }
    }
    return parseError("unrecognized type syntax", path);  
}

// jlist is a list starting with k
function parseCompoundType(t:Env env, Binding? b, string k, json[] jlist, Path parent) returns t:SemType|ParseError {
    match k {
        "|" => {
            t:SemType[] v = check parseTypes(env, b, jlist, parent, 1);
            return reduce(v, t:union, t:NEVER);
        }
        "&" => {
            t:SemType[] v = check parseTypes(env, b, jlist, parent, 1);
            return reduce(v, t:intersect, t:TOP);
        }
        "tuple" => {
            t:SemType? s = lookupDef(env, b, jlist);
            if !(s is ()) {
                return s;
            }
            t:ListDefinition def = new;
            t:SemType[] members = check parseTypes(env, consDefBinding(jlist, def, b), jlist, parent, 1);
            return def.define(env, members, t:NEVER);
        }
        "list" => {
            t:SemType? s = lookupDef(env, b, jlist);
            if !(s is ()) {
                return s;
            }
            t:ListDefinition def = new;
            t:SemType[] members = check parseTypes(env, consDefBinding(jlist, def, b), jlist, parent, 1);
            t:SemType rest;
            if members.length() == 0 {
                rest = t:TOP;
            }
            else {
                rest = members.pop();
            }
            return def.define(env, members, rest);
        }
        "record" => {
            t:SemType? s = lookupDef(env, b, jlist);
            if !(s is ()) {
                return s;
            }
            t:MappingDefinition def = new;
            t:Field[] fields = check parseFields(env, consDefBinding(jlist, def, b), jlist, parent, 1);
            return def.define(env, fields, t:NEVER);
        }
        "map" => {
            t:SemType? s = lookupDef(env, b, jlist);
            if !(s is ()) {
                return s;
            }
            if jlist.length() == 1 {
                return parseError("'map' must be followed by a type", parent, 0);
            }
            t:MappingDefinition def = new;
            Binding? mb = consDefBinding(jlist, def, b);
            t:SemType rest = check parseType(env, mb, jlist[1], pathAppend(parent, 1));
            t:Field[] fields = check parseFields(env, mb, jlist, parent, 2);
            return def.define(env, fields, rest);
        }
        "function" => {
            t:SemType? s = lookupDef(env, b, jlist);
            if !(s is ()) {
                return s;
            }
            t:FunctionDefinition def = new(env);
            t:SemType[] v = check parseTypes(env, consDefBinding(jlist, def, b), jlist, parent, 1);
            if v.length() == 0 {
                return t:FUNCTION;
            }
            t:SemType ret = v.pop();
            return def.define(env, t:tuple(env, ...v), ret);
        }
        "error" => {
            if jlist.length() != 2 {
                return parseError("'error' must be followed by a string", parent, 0);
            }
            t:SemType detail = check parseType(env, b, jlist[1], pathAppend(parent, 1));
            return t:errorDetail(detail);
        }
        "string" => {
            if jlist.length() != 2 {
                return parseError("'string' must be followed by a string", parent, 0);
            }
            final json value = jlist[1];
            if !(value is string) {
                return parseError("'string' must be followed by a string", parent, 1);
            }
            else {
                return t:stringConst(value);
            }
        }
        "int" => {
            if jlist.length() != 2 {
                return parseError("'int' must be followed by a string", parent, 0);
            }
            final json value = jlist[1];
            final int intValue;
            if value is int {
                intValue = value;
            }
            else if value is string {
                var res = int:fromString(value);
                if res is error {
                    return parseError("not an integer", parent, 1);
                }
                else {
                    intValue = res;
                }
            }
            else {
                return parseError("'int' must be followed by a string", parent, 1);
            }
            return t:intConst(intValue);
        }
        "float" => {
            if jlist.length() != 2 {
                return parseError("'float' must be followed by a value", parent, 0);
            }
            final json value = jlist[1];
            final float floatValue;
            if value is string {
                var res = float:fromString(value);
                if res is error {
                    return parseError("not a float: " + value, parent, 1);
                }
                else {
                    floatValue = res;
                }
            }
            else {
                return parseError("'float' must be followed by a string with digits", parent, 1);
            }
            return t:floatConst(floatValue);
        }
        "rec" => {
            if jlist.length() == 1 {
                return parseError("'rec' must be followed by a string and a type", parent, 0);
            }
            final json name = jlist[1];
            if !(name is string) {
                return parseError("'rec' must be followed by a string", parent, 1);
            }
            else {
                if jlist.length() == 3 {
                    return parseRec(env, b, name, jlist[2], pathAppend(parent, 2));
                }
                else {
                    return parseError("'rec' must be followed by two operands",
                                      parent, jlist.length() > 3 ? 3 : 1);
                }
            }
        }
        "ref" => {
            if jlist.length() != 2 {
                return parseError("'ref' must be followed by a string", parent, 0);
            }
            final json name = jlist[1];
            if !(name is string) {
                return parseError("'ref' must be followed by a string", parent, 1);
            }
            else {
                var res = lookupBinding(b, name);
                if res is () {
                    return parseError("no binding for '" + name + "'", parent, 1);
                }
                else if res is "loop" {
                    return parseError("invalid recursion for '" + name + "'", parent, 1);
                }
                else {
                    var [j, path] = res;
                    return parseType(env, b, j, path);
                }
            }
        }
    }
    return parseError("unrecognized keyword '" + k + "'", pathAppend(parent, 0));
}

function consDefBinding(json desc, t:Definition def, Binding? next) returns Binding? {
    if next is () {
        return next;
    }
    DefBinding db = { desc, def, next };
    return db;
}

function parseFields(t:Env env, Binding? b, json[] jlist, Path parent, int startIndex) returns t:Field[]|ParseError {
    t:Field[] fields = [];
    foreach int i in startIndex ..< jlist.length() {
        t:Field f = check parseField(env, b, jlist[i], pathAppend(parent, i));
        fields.push(f);
    }
    return fields;
}


function parseField(t:Env env, Binding? b, json j, Path path) returns t:Field|ParseError {
    if !(j is json[]) || j.length() != 2 {
        return parseError("field must be 2-tuple", path);
    }
    else {
        json name = j[0];
        if !(name is string) {
            return parseError("first member of field must be a string", path, 0);
        }
        else {
            t:SemType t = check parseType(env, b, j[1], pathAppend(path, 1));
            return [name, t];
        }
    }
}

function lookupBinding(Binding? b, string name) returns [json, Path]|"loop"? {
    Binding? tem = b;
    boolean loop = true;
    while true {
        if tem is () {
           break;
        }
        else if tem is NameBinding {
            if tem.name == name {
                if loop {
                    return "loop";
                }
                return [tem.desc, tem.path];
            }
            tem = tem.next;
        }
        else {
            loop = false;
            tem = tem.next;
        }
    }
    return ();
}

function lookupDef(t:Env env, Binding? b, json desc) returns t:SemType? {
    Binding? tem = b;
    while true {
        if tem is () {
            break;
        }
        else if tem is DefBinding {
            if tem.desc === desc {
                return tem.def.getSemType(env);
            }
            tem = tem.next;
        }
        else {
            tem = tem.next;
        }
    }
    return ();
}

function parseRec(t:Env env, Binding? b, string name, json t, Path path) returns t:SemType|ParseError {
    NameBinding nb = { name, next: b, desc: t, path };
    return parseType(env, nb, t, path);
}

function parseTypes(t:Env env, Binding? b, json[] js, Path parent, int startIndex) returns t:SemType[]|ParseError {
    t:SemType[] s = [];
    foreach var i in startIndex ..< js.length() {
        t:SemType t = check parseType(env, b, js[i], pathAppend(parent, i));
        s.push(t);
    }
    return s;
}

function parseError(string message, Path path, int? step = ()) returns ParseError {
    if !(step is ()) {
        path.push(step);
    }
    return error ParseError(message, path=path);
}

function reduce(t:SemType[] v, function (t:SemType, t:SemType) returns t:SemType binary, t:SemType initial) returns t:SemType {
    match v.length() {
        0 => { return initial; }
        1 => { return v[0]; }
    }
    t:SemType result = binary(v[0], v[1]);
    foreach int i in 2 ..< v.length() {
        result = binary(result, v[i]);
    }
    return result;
}

function pathAppend(Path parent, int index) returns Path {
    Path path = parent.clone();
    path.push(index);
    return path;
}
