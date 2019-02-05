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
    if (bType is BUnionType) {
        return serializeTypes(bType.members, "|");
    } else if (bType is BInvokableType) {
        return "function (" + serializeTypes(bType.paramTypes, ",") +
                                                    ") returns " + serialize(bType.retType);
    } else if (bType is BArrayType) {
        return serialize(bType.eType) + "[]";
    }

    error err = error("Unsupported type serializtion ");
    panic err;
}

function serializeTypes(BType[] bTypes, string delimiter) returns string {
    var result = "";
    boolean first = true;
    foreach var bType in bTypes {
        if (!first){
            result = result + delimiter;
        }
        result = result + serialize(bType);
        first = false;
    }
    return result;
}
