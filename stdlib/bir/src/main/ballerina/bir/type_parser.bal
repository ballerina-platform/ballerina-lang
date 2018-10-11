import ballerina/io;

public type TypeParser object {
    BirChannelReader reader;
    public int TYPE_TAG_NIL = 0;
    public int TYPE_TAG_INT = 1;
    public int TYPE_TAG_BYTE = 2;
    public int TYPE_TAG_FLOAT = 3;
    public int TYPE_TAG_STRING = 4;
    public int TYPE_TAG_BOOLEAN = 5;
    public int TYPE_TAG_UNION = 10;
    public int TYPE_TAG_ARRAY = 11;
    public int TYPE_TAG_INVOKABL_TYPE = 12;

    public new(reader) {
    }

    public function parseType() returns BType {
        var typeTag = reader.readInt8();
        if (typeTag == TYPE_TAG_NIL ){
            return "()";
        } else if (typeTag == TYPE_TAG_INT){
            return "int";
        } else if (typeTag == TYPE_TAG_BYTE){
            return "byte";
        } else if (typeTag == TYPE_TAG_FLOAT){
            return "float";
        } else if (typeTag == TYPE_TAG_STRING){
            return "string";
        } else if (typeTag == TYPE_TAG_BOOLEAN){
            return "boolean";
        } else if (typeTag == TYPE_TAG_UNION){
            return parseUnionType();
        } else if (typeTag == TYPE_TAG_ARRAY){
            return parseArrayType();
        } else if (typeTag == TYPE_TAG_INVOKABL_TYPE){
            return parseInvokableType();
        }
        error err = { message: "Unknown type tag :" + typeTag };
        throw err;
    }

    function parseArrayType() returns BArrayType {
        return { eType:parseType() };
    }

    function parseUnionType() returns BUnionType {
        return { members:parseTypes() };
    }

    function parseInvokableType() returns BInvokableType {
        return { paramTypes:parseTypes(), retType: parseType() };
    }

    function parseTypes() returns BType[] {
        int count = reader.readInt32();
        int i;

        BType[] types = [];
        while (i < count) {
            types[lengthof types] = parseType();
            i++;
        }
        return types;
    }
};
