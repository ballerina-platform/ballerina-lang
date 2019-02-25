import ballerina/io;
import ballerina/log;

public type Person record {
    string name = "";
    int age = 0;
    float income = 0.0;
    boolean isMarried = false;
    !...;
};

// Serialize record into binary.
function serialize(Person p, io:WritableByteChannel byteChannel) {
    io:WritableDataChannel dc = new io:WritableDataChannel(byteChannel);
    var length = p.name.toByteArray("UTF-8").length();
    var lengthResult = dc.writeInt32(length);
    var nameResult = dc.writeString(p.name, "UTF-8");
    var ageResult = dc.writeInt16(p.age);
    var incomeResult = dc.writeFloat64(p.income);
    var maritalStatusResult = dc.writeBool(p.isMarried);
    var closeResult = dc.close();
}

//Deserialize record from binary.
function deserialize(io:ReadableByteChannel byteChannel) returns Person {
    Person person = {};
    int nameLength = 0;
    string nameValue;
    io:ReadableDataChannel dc = new io:ReadableDataChannel(byteChannel);
    //Read 32 bit signed integer.
    var int32Result = dc.readInt32();
    if (int32Result is int) {
        nameLength = int32Result;
    } else {
        log:printError("Error occurred while reading name length",
                        err = int32Result);
    }
    //Read UTF-8 encoded string represented through specified amount of bytes.
    var strResult = dc.readString(nameLength, "UTF-8");
    if (strResult is string) {
        person.name = strResult;
    } else {
        log:printError("Error occurred while reading name", err = strResult);
    }
    //Read 16 bit signed integer.
    var int16Result = dc.readInt16();
    if (int16Result is int) {
        person.age = int16Result;
    } else {
        log:printError("Error occurred while reading age",
                        err = int16Result);
    }
    //Read 64 bit signed float.
    var float64Result = dc.readFloat64();
    if (float64Result is float) {
        person.income = float64Result;
    } else {
        log:printError("Error occurred while reading income",
                        err = float64Result);
    }
    //Read boolean.
    var boolResult = dc.readBool();
    if (boolResult is boolean) {
        person.isMarried = boolResult;
    } else {
        log:printError("Error occurred while reading marital status",
                        err = boolResult);
    }
    //Finally close the data channel.
    var closeResult = dc.close();
    return person;
}

//Serialize and write record to a file.
function writeRecordToFile(Person p, string path) {
    io:WritableByteChannel wc = io:openWritableFile(path);
    serialize(p, wc);
}

//Read serialized record from file.
function readRecordFromFile(string path) returns Person {
    io:ReadableByteChannel rc = io:openReadableFile(path);
    return deserialize(rc);
}

public function main() {
    Person wPerson = { name: "Ballerina", age: 21,
                       income: 1543.12, isMarried: true };
    //Write record to file.
    writeRecordToFile(wPerson, "./files/person.bin");
    io:println("Person record successfully written to file");
    //Read record from file.
    Person rPerson = readRecordFromFile("./files/person.bin");
    io:println("Reading person record from file");
    io:println(rPerson);
}
