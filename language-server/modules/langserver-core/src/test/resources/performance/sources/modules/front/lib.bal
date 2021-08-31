import wso2/nballerina.bir;
import wso2/nballerina.types as t;

final [string, string[], string, readonly & t:SemType[], t:SemType][] libFunctions = [
    ["ballerina", ["io"], "println", [t:TOP], t:NIL],
    ["ballerina", ["lang", "string"], "length", [t:STRING], t:INT],
    ["ballerina", ["lang", "array"], "length", [t:LIST], t:INT],
    ["ballerina", ["lang", "array"], "push", [t:LIST, t:ANY], t:NIL],
    ["ballerina", ["lang", "map"], "length", [t:MAPPING], t:INT],
    ["ballerina", ["lang", "int"], "toHexString", [t:INT], t:STRING]
];

function getLibFunction(bir:ModuleId id, string name) returns bir:FunctionSignature? {
    foreach var [org, moduleNameParts, functionName, paramTypes, returnType] in libFunctions {
        if id.organization == org && id.names == moduleNameParts && name == functionName {
            return { returnType, paramTypes };
        }   
    }
    return ();
}
