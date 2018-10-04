import ballerina/io;

public function serialize(BType bType) returns string {
    if (bType == "()"){
        return "()";
    } else if (bType == "int"){
        return "int";
    } else if (bType == "byte"){
        return "byte";
    } else if (bType == "boolean"){
        return "boolean";
    } else if (bType == "float"){
        return "float";
    } else if (bType == "string"){
        return "string";
    }
    match (bType){
        BUnionType bUnionType => return serializeTypes(bUnionType.members, "|");
        BInvokableType bInvokableType => return "function (" + serializeTypes(bInvokableType.paramTypes, ",") +
                                                ") returns " + serialize(bInvokableType.retType);
        BArrayType bArrayType => return serialize(bArrayType.eType) + "[]";
        any _ => {}
    }

    error err = { message: "Unsupported type serializtion " };
    throw err;
}

function serializeTypes(BType[] bTypes, string delimiter) returns string {
    var result = "";
    boolean first = true;
    foreach bType in bTypes {
        if (!first){
            result = result + delimiter;
        }
        result = result + serialize(bType);
        first = false;
    }
    return result;
}
