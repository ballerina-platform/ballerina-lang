import ballerina/io;

public type BirChannelReader object {
    ChannelReader reader;
    ConstPool cp;

    public new(reader, cp){
    }

    public function readBType() returns BType {
        string sginatureAlias = self.readStringCpRef();
        if (sginatureAlias == "I"){
            return "int";
        } else if (sginatureAlias == "B"){
            return "boolean";
        }
        error err = error("type signature " + sginatureAlias + " not supported.");
        panic err;
    }

    public function readStringCpRef() returns string {
        return self.cp.strings[self.reader.readInt32()];
    }

    public function readIntCpRef() returns int {
        return self.cp.ints[self.reader.readInt32()];
    }




    // following methods "proxied" since ballerina doesn't support obj inheritance yet

    public function readBoolean() returns boolean {
        return self.reader.readBoolean();
    }

    public function readInt8() returns int {
        return self.reader.readInt8();
    }

    public function readInt32() returns int {
        return self.reader.readInt32();
    }

    public function readInt64() returns int {
        return self.reader.readInt64();
    }


    public function readString() returns string {
        return self.reader.readString();
    }

    public function readByteArray(int len) returns byte[] {
        return self.reader.readByteArray(len);
    }
};
