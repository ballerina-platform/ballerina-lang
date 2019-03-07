import ballerina/io;

public type TypeParser object {
    BirChannelReader reader;

    public int TYPE_TAG_INT = 1;
    public int TYPE_TAG_BYTE = TYPE_TAG_INT + 1;
    public int TYPE_TAG_FLOAT = TYPE_TAG_BYTE + 1;
    public int TYPE_TAG_DECIMAL = TYPE_TAG_FLOAT + 1;
    public int TYPE_TAG_STRING = TYPE_TAG_DECIMAL + 1;
    public int TYPE_TAG_BOOLEAN = TYPE_TAG_STRING + 1;
    // All the above types are values type
    public int TYPE_TAG_JSON = TYPE_TAG_BOOLEAN + 1;
    public int TYPE_TAG_XML = TYPE_TAG_JSON + 1;
    public int TYPE_TAG_TABLE = TYPE_TAG_XML + 1;
    public int TYPE_TAG_NIL = TYPE_TAG_TABLE + 1;
    public int TYPE_TAG_ANYDATA = TYPE_TAG_NIL + 1;
    public int TYPE_TAG_RECORD = TYPE_TAG_ANYDATA + 1;
    public int TYPE_TAG_TYPEDESC = TYPE_TAG_RECORD + 1;
    public int TYPE_TAG_STREAM = TYPE_TAG_TYPEDESC + 1;
    public int TYPE_TAG_MAP = TYPE_TAG_STREAM + 1;
    public int TYPE_TAG_INVOKABLE = TYPE_TAG_MAP + 1;
    // All the above types are branded types
    public int TYPE_TAG_ANY = TYPE_TAG_INVOKABLE + 1;
    public int TYPE_TAG_ENDPOINT = TYPE_TAG_ANY + 1;
    public int TYPE_TAG_SERVICE = TYPE_TAG_ENDPOINT + 1;
    public int TYPE_TAG_ARRAY = TYPE_TAG_SERVICE + 1;
    public int TYPE_TAG_UNION = TYPE_TAG_ARRAY + 1;
    public int TYPE_TAG_PACKAGE = TYPE_TAG_UNION + 1;
    public int TYPE_TAG_NONE = TYPE_TAG_PACKAGE + 1;
    public int TYPE_TAG_VOID = TYPE_TAG_NONE + 1;
    public int TYPE_TAG_XMLNS = TYPE_TAG_VOID + 1;
    public int TYPE_TAG_ANNOTATION = TYPE_TAG_XMLNS + 1;
    public int TYPE_TAG_XML_ATTRIBUTES = TYPE_TAG_ANNOTATION + 1;
    public int TYPE_TAG_SEMANTIC_ERROR = TYPE_TAG_XML_ATTRIBUTES + 1;
    public int TYPE_TAG_ERROR = TYPE_TAG_SEMANTIC_ERROR + 1;
    public int TYPE_TAG_ITERATOR = TYPE_TAG_ERROR + 1;
    public int TYPE_TAG_TUPLE = TYPE_TAG_ITERATOR + 1;
    public int TYPE_TAG_FUTURE = TYPE_TAG_TUPLE + 1;
    public int TYPE_TAG_INTERMEDIATE_COLLECTION = TYPE_TAG_FUTURE + 1;
    public int TYPE_TAG_FINITE = TYPE_TAG_INTERMEDIATE_COLLECTION + 1;
    public int TYPE_TAG_OBJECT = TYPE_TAG_FINITE + 1;
    public int TYPE_TAG_BYTE_ARRAY = TYPE_TAG_OBJECT + 1;
    public int TYPE_TAG_FUNCTION_POINTER = TYPE_TAG_BYTE_ARRAY + 1;
    public int TYPE_TAG_CHANNEL = TYPE_TAG_BYTE_ARRAY + 1;

    public function __init(BirChannelReader reader) {
        self.reader = reader;
    }

    public function parseType() returns BType {
        var typeTag = self.reader.readInt8();
        if (typeTag == self.TYPE_TAG_ANY){
            return "any";
        } else if (typeTag == self.TYPE_TAG_ANYDATA ){
            return "anydata";
        } else if (typeTag == self.TYPE_TAG_NONE ){
            return "none";
        } else if (typeTag == self.TYPE_TAG_NIL ){
            return "()";
        } else if (typeTag == self.TYPE_TAG_INT){
            return "int";
        } else if (typeTag == self.TYPE_TAG_BYTE){
            return "byte";
        } else if (typeTag == self.TYPE_TAG_FLOAT){
            return "float";
        } else if (typeTag == self.TYPE_TAG_STRING){
            return "string";
        } else if (typeTag == self.TYPE_TAG_BOOLEAN){
            return "boolean";
        } else if (typeTag == self.TYPE_TAG_UNION){
            return self.parseUnionType();
        } else if (typeTag == self.TYPE_TAG_TUPLE){
            return self.parseTupleType();
        } else if (typeTag == self.TYPE_TAG_ARRAY){
            return self.parseArrayType();
        } else if (typeTag == self.TYPE_TAG_MAP){
            return self.parseMapType();
        } else if (typeTag == self.TYPE_TAG_INVOKABLE){
            return self.parseInvokableType();
        } else if (typeTag == self.TYPE_TAG_RECORD){
            return self.parseRecordType();
        } else if (typeTag == self.TYPE_TAG_OBJECT){
            return self.parseObjectType();
        } else if (typeTag == self.TYPE_TAG_ERROR){
            return self.parseErrorType();
        }
        error err = error("Unknown type tag :" + typeTag);
        panic err;
    }

    function parseArrayType() returns BArrayType {
        return { state:self.parseArrayState(), eType:self.parseType() };
    }

    function parseMapType() returns BMapType {
        return { constraint:self.parseType() };
    }

    function parseUnionType() returns BUnionType {
        return { members:self.parseTypes() };
    }

    function parseTupleType() returns BTupleType {
        return { tupleTypes:self.parseTypes() };
    }

    function parseInvokableType() returns BInvokableType {
        return { paramTypes:self.parseTypes(), retType: self.parseType() };
    }

    function parseRecordType() returns BRecordType {
        return { name:{value:self.reader.readStringCpRef()}, sealed:self.reader.readBoolean(), 
                    restFieldType: self.parseType(), fields: self.parseRecordFields() };
    }

    function parseRecordFields() returns BRecordField[] {
        int size = self.reader.readInt32();
        int c = 0;
        BRecordField[] fields = [];
        while c < size {
            fields[c] = self.parseRecordField();
            c = c + 1;    
        }
        return fields;
    }

    function parseRecordField() returns BRecordField {
        return {name:{value:self.reader.readStringCpRef()}, typeValue:self.parseType()};
    }

    function parseObjectType() returns BObjectType {
        return { name:{value:self.reader.readStringCpRef()}, fields: self.parseObjectFields() };
    }

    function parseObjectFields() returns BObjectField[] {
        int size = self.reader.readInt32();
        int c = 0;
        BObjectField[] fields = [];
        while c < size {
            fields[c] = self.parseObjectField();
            c = c + 1;    
        }
        return fields;
    }

    function parseObjectField() returns BObjectField {
        return {name:{value:self.reader.readStringCpRef()}, visibility:parseVisibility(self.reader), typeValue:self.parseType()};
    }

    function parseErrorType() returns BErrorType {
        return {reasonType:self.parseType(), detailType:self.parseType()};
    }

    function parseTypes() returns BType[] {
        int count = self.reader.readInt32();
        int i = 0;

        BType[] types = [];
        while (i < count) {
            types[types.length()] = self.parseType();
            i = i + 1;
        }
        return types;
    }

    function parseArrayState() returns ArrayState {
        int b = self.reader.readInt8();
        if (b == 1) {
            return "CLOSED_SEALED";
        } else if (b == 2) {
            return "OPEN_SEALED";
        } else if (b == 3) {
            return "UNSEALED";
        } 
        error err = error("unknown array state tag " + b);
        panic err;
    }
};
