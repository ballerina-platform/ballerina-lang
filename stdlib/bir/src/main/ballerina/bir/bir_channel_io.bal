import ballerina/io;

public type BirChannelReader object {
    ChannelReader reader;
    ConstPool cp;

    public new(reader, cp){
    }

    public function readBType() returns BType {
        string sginatureAlias = readStringCpRef();
        if (sginatureAlias == "I"){
            return "int";
        } else if (sginatureAlias == "B"){
            return "boolean";
        }
        error err = { message: "type signature " + sginatureAlias + " not supported." };
        throw err;
    }

    public function readStringCpRef() returns string {
        return cp.strings[reader.readInt32()];
    }

    public function readIntCpRef() returns int {
        return cp.ints[reader.readInt32()];
    }




    // following methods "proxied" since ballerina doesn't support obj inheritance yet

    public function readBoolean() returns boolean {
        return reader.readBoolean();
    }

    public function readInt8() returns int {
        return reader.readInt8();
    }

    public function readInt32() returns int {
        return reader.readInt32();
    }

    public function readInt64() returns int {
        return reader.readInt64();
    }


    public function readString() returns string {
        return reader.readString();
    }

    public function readByteArray(int len) returns byte[] {
        return reader.readByteArray(len);
    }
};
