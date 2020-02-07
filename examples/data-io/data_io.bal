import ballerina/io;
import ballerina/log;

public type Person record {|
    string name = "";
    int age = 0;
    float income = 0.0;
    boolean isMarried = false;
|};

// Serializes the record into binary.
function serialize(Person p, io:WritableByteChannel byteChannel) {
    io:WritableDataChannel dc = new io:WritableDataChannel(byteChannel);
    var length = p.name.toBytes().length();
    var lengthResult = dc.writeInt32(length);
    var nameResult = dc.writeString(p.name, "UTF-8");
    var ageResult = dc.writeInt16(p.age);
    var incomeResult = dc.writeFloat64(p.income);
    var maritalStatusResult = dc.writeBool(p.isMarried);
    var closeResult = dc.close();
}

//Deserializes the record from binary.
function deserialize(io:ReadableByteChannel byteChannel) returns Person {
    Person person = {};
    int nameLength = 0;
    string nameValue;
    io:ReadableDataChannel dc = new io:ReadableDataChannel(byteChannel);
    //Reads a 32-bit-signed integer.
    var int32Result = dc.readInt32();
    if (int32Result is int) {
        nameLength = int32Result;
    } else {
        log:printError("Error occurred while reading name length",
                        err = int32Result);
    }
    //Reads a UTF-8-encoded string represented through the specified amounts of bytes.
    var strResult = dc.readString(nameLength, "UTF-8");
    if (strResult is string) {
        person.name = strResult;
    } else {
        log:printError("Error occurred while reading name", err = strResult);
    }
    //Reads a 16-bit-signed integer.
    var int16Result = dc.readInt16();
    if (int16Result is int) {
        person.age = int16Result;
    } else {
        log:printError("Error occurred while reading age",
                        err = int16Result);
    }
    //Reads a 64-bit-signed float.
    var float64Result = dc.readFloat64();
    if (float64Result is float) {
        person.income = float64Result;
    } else {
        log:printError("Error occurred while reading income",
                        err = float64Result);
    }
    //Reads a boolean.
    var boolResult = dc.readBool();
    if (boolResult is boolean) {
        person.isMarried = boolResult;
    } else {
        log:printError("Error occurred while reading marital status",
                        err = boolResult);
    }
    //Finally closes the data channel.
    var closeResult = dc.close();
    return person;
}

//Serializes and writes the record to a file.
function writeRecordToFile(Person p, string path) returns error? {
    io:WritableByteChannel wc = check io:openWritableFile(path);
    serialize(p, wc);
}

//Reads the serialized record from the file.
function readRecordFromFile(string path) returns @tainted Person | error {
    io:ReadableByteChannel rc = check io:openReadableFile(path);
    return deserialize(rc);
}

public function main() returns error? {
    Person wPerson = {
        name: "Ballerina",
        age: 21,
        income: 1543.12,
        isMarried: true
    };
    //Writes the record to a file.
    check writeRecordToFile(wPerson, "./files/person.bin");
    io:println("Person record successfully written to file");
    //Reads record from a file.
    Person rPerson = check readRecordFromFile("./files/person.bin");
    io:println("Reading person record from file");
    io:println(rPerson);
}
