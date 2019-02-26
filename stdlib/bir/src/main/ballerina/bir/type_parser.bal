import ballerina/io;

public type TypeParser object {
    BirChannelReader reader;
    public int TYPE_TAG_NIL = 10;
    public int TYPE_TAG_INT = 1;
    public int TYPE_TAG_BYTE = 2;
    public int TYPE_TAG_FLOAT = 3;
    public int TYPE_TAG_DECIMAL = 4;
    public int TYPE_TAG_STRING = 5;
    public int TYPE_TAG_BOOLEAN = 6;
    public int TYPE_TAG_UNION = 21;
    public int TYPE_TAG_ARRAY = 20;
    public int TYPE_TAG_INVOKABL_TYPE = 16;
    public int TYPE_TAG_RECORD_TYPE = 12;
    public int TYPE_TAG_OBJECT_TYPE = 35;

    public function __init(BirChannelReader reader) {
        self.reader = reader;
    }

    public function parseType() returns BType {
        var typeTag = self.reader.readInt8();
        if (typeTag == self.TYPE_TAG_NIL ){
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
        } else if (typeTag == self.TYPE_TAG_ARRAY){
            return self.parseArrayType();
        } else if (typeTag == self.TYPE_TAG_INVOKABL_TYPE){
            return self.parseInvokableType();
        } else if (typeTag == self.TYPE_TAG_RECORD_TYPE){
            return self.parseRecordType();
        } else if (typeTag == self.TYPE_TAG_OBJECT_TYPE){
            return self.parseObjectType();
        }
        error err = error("Unknown type tag :" + typeTag);
        panic err;
    }

    function parseArrayType() returns BArrayType {
        return { eType:self.parseType() };
    }

    function parseUnionType() returns BUnionType {
        return { members:self.parseTypes() };
    }

    function parseInvokableType() returns BInvokableType {
        return { paramTypes:self.parseTypes(), retType: self.parseType() };
    }

    function parseRecordType() returns BRecordType {
        return { sealed:self.reader.readBoolean(), restFieldType: self.parseType(), fields: self.parseRecordFields() };
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
        return { fields: self.parseObjectFields() };
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
};
