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
