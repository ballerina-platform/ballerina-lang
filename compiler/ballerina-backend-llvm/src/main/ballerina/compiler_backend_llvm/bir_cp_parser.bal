import ballerina/io;

type ConstPool record {
    PackageId[] packages;
    string[] strings;
    int[] ints;
};

function parseCp(ChannelReader reader) returns ConstPool {
    var cpCount = reader.readInt32();
    ConstPool cp;
    int i = 0;
    while (i < cpCount) {
        var cpType = reader.readInt8();
        if (cpType == 1){
            cp.ints[i] = reader.readInt64();
        } else if (cpType == 4){
            cp.strings[i] = reader.readString();
        } else if (cpType == 5){
            PackageId id = { org: cp.strings[reader.readInt32()],
                name: <string>cp.strings[reader.readInt32()],
                varstionVallue: <string>cp.strings[reader.readInt32()] };
            cp.packages[i] = id;
        } else {
            error err = { message: "cp type " + cpType + " not supported.:" };
            throw err;
        }
        i++;
    }
    return cp;
}


