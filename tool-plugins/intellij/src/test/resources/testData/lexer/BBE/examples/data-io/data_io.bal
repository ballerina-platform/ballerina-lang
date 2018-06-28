import ballerina/io;
import ballerina/log;

public type Person record {
    string name;
    int age;
    float income;
    boolean isMarried;
};

//Serialize record into binary
function serialize(Person p, io:ByteChannel channel) {
    io:DataChannel dc = new io:DataChannel(channel);
    var length = lengthof p.name.toBlob("UTF-8");
    var lengthResult = dc.writeInt32(length);
    var nameResult = dc.writeString(p.name, "UTF-8");
    var ageResult = dc.writeInt16(p.age);
    var incomeResult = dc.writeFloat64(p.income);
    var maritalStatusResult = dc.writeBool(p.isMarried);
    var closeResult = dc.close();
}

//Deserialize record into binary
function deserialize(io:ByteChannel channel) returns Person {
    Person person;
    int nameLength;
    string nameValue;
    io:DataChannel dc = new io:DataChannel(channel);
    //Read 32 bit singed integer
    match dc.readInt32() {
        int namel => nameLength = namel;
        error e =>
        log:printError("Error occurred while reading name length",
            err = e);
    }
    //Read UTF-8 encoded string represented through specified amount of bytes
    match dc.readString(nameLength, "UTF-8") {
        string name => person.name = name;
        error e =>
        log:printError("Error occurred while reading name",
            err = e);
    }
    //Read 16 bit signed integer
    match dc.readInt16() {
        int age => person.age = age;
        error e =>
        log:printError("Error occurred while reading age",
            err = e);
    }
    //Read 64 bit signed float
    match dc.readFloat64() {
        float income => person.income = income;
        error e =>
        log:printError("Error occurred while reading income",
            err = e);
    }
    //Read boolean
    match dc.readBool() {
        boolean isMarried => person.isMarried = isMarried;
        error e =>
        log:printError("Error occurred while reading marital status",
            err = e);
    }
    //Finally close the data channel
    var closeResult = dc.close();
    return person;
}

//Serialize and write record to a file
function writeRecordToFile(Person p, string path) {
    io:ByteChannel wc = io:openFile(path, io:WRITE);
    serialize(p, wc);
}

//Read serialized record from file
function readRecordFromFile(string path) returns Person {
    io:ByteChannel rc = io:openFile(path, io:READ);
    return deserialize(rc);
}

function main(string...args) {
    Person wPerson = {name:"Ballerina", age:21, income:1543.12, isMarried:true};
    //Write record to file
    writeRecordToFile(wPerson, "./files/person.bin");
    io:println("Person record successfully written to file");
    //Read record from file
    Person rPerson = readRecordFromFile("./files/person.bin");
    io:println("Reading person record from file");
    io:println(rPerson);
}
