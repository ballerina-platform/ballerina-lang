import ballerina/io;
type BirChannelReader object {
    ChannelReader reader;
    ConstPool cp;

    new(reader, cp){
    }

    function readBType() returns BType {
        string sginatureAlias = readStringCpRef();
        if (sginatureAlias == "I"){
            return "int";
        } else if (sginatureAlias == "B"){
            return "boolean";
        }
        error err = { message: "type signature " + sginatureAlias + " not supported." };
        throw err;
    }

    function readStringCpRef() returns string {
        return cp.strings[reader.readInt32()];
    }

    function readIntCpRef() returns int {
        return cp.ints[reader.readInt32()];
    }




    // following methods "proxied" since ballerina doesn't support obj inheritance yet

    function readBoolean() returns boolean {
        return reader.readBoolean();
    }

    function readInt8() returns int {
        return reader.readInt8();
    }

    function readInt32() returns int {
        return reader.readInt32();
    }

    function readInt64() returns int {
        return reader.readInt64();
    }


    function readString() returns string {
        return reader.readString();
    }

    function readByteArray(int len) returns byte[] {
        return reader.readByteArray(len);
    }
};
