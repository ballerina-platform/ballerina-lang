import ballerina/io;

public function serialize(BType bType) returns string {
    if (bType is BTypeNil){
        return "()";
    } else if (bType is BTypeInt){
        return "int";
    } else if (bType is BTypeByte){
        return "byte";
    } else if (bType is BTypeBoolean){
        return "boolean";
    } else if (bType is BTypeFloat){
        return "float";
    } else if (bType is BTypeString){
        return "string";
    } else if (bType is BUnionType) {
        return serializeTypes(bType.members, "|");
    } else if (bType is BInvokableType) {
        return "function (" + serializeTypes(bType.paramTypes, ", ") + ") returns " + serialize(bType.retType);
    } else if (bType is BArrayType) {
        return serialize(bType.eType) + "[]";
    } else if (bType is BObjectType) {
        return "object {" + serializeFields(bType.fields) + serializeAttachedFunc(bType.attachedFunctions) + "}";
    }

    error err = error(io:sprintf("Unsupported serialization for type '%s'", bType));
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

function serializeFields(BObjectField[] fields) returns string {
    var result = "";
    var delimiter = "; ";
    foreach var field in fields {
        result = result + serialize(field.typeValue) + " " + field.name.value + delimiter;
    }
    return result;
}

function serializeAttachedFunc(BAttachedFunction[] functions) returns string {
    var result = "";
    foreach var func in functions {
        result = result + serialize(func.funcType) + " " + func.name.value + "; ";
    }
    return result;
}
