public type ConstPool record {
    PackageId[] packages;
    string[] strings;
    int[] ints;
};

public type ConstPoolParser object {
    ChannelReader reader;
    ConstPool cp;
    int i;

    public new(reader) {
        i = 0;
    }

    public function parse() returns ConstPool {
        var cpCount = reader.readInt32();
        while (i < cpCount) {
            parseConstPoolEntry();
            i++;
        }
        return cp;
    }

    public function parseConstPoolEntry() {
        var cpType = reader.readInt8();

        if (cpType == 1){
            parseInt();
        } else if (cpType == 4){
            parseString();
        } else if (cpType == 5){
            parsePackageId();
        } else {
            error err = { message: "cp type " + cpType + " not supported.:" };
            throw err;
        }
    }

    function parseInt() {
        cp.ints[i] = reader.readInt64();
    }

    function parseString() {
        cp.strings[i] = reader.readString();
    }

    function parsePackageId() {
        PackageId id = { org: cp.strings[reader.readInt32()],
            name: <string>cp.strings[reader.readInt32()],
            varstionVallue: <string>cp.strings[reader.readInt32()] };
        cp.packages[i] = id;
    }

};

