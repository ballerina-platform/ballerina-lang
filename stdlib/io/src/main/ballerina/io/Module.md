## Module Overview

This module is designed to support input and output operations via channels in a canonical way, either in a blocking,
or non-blocking manner.

![architecture](resources/package-architecture.svg)

### Channels
A channel represents an I/O source or sink of some bytes, characters, or records that are opened for reading, or
writing, respectively.
### Byte channels
The most primitive channel is the `ByteChannel` which reads and writes 8-bit bytes.

```ballerina
// Open a file in read mode.
io:ReadableByteChannel readableByteChannel = io:openReadableFile("some/file.txt");

// Here is how 100 bytes are read from the channel.
(byte[], int)|error result = readableByteChannel.read(100);

// Open a file in write mode.
io:WritableByteChannel writableByteChannel = io:openWritableFile("some/file.txt");

// Write some content to the beginning of the file.
string someContent = "some content";
byte[] content = someContent.toByteArray("UTF-8");
int|error result = writableByteChannel.write(content, 0);

```
### Character channels
The `CharacterChannel` is used to read and write characters. The charset encoding is specified when creating the
`CharacterChannel`.

 ```ballerina
// Create a `ReadableCharacterChannel` from the `ReadableByteChannel`.
var readableCharChannel = new io:ReadableCharacterChannel(readableByteChannel, "UTF-8");
```

If a `ReadableCharacterChannel` points to a JSON or XML source, it can be read and then written, directly into a variable of
the respective type.

```ballerina
// Reading a JSON.
json|error result = readableCharChannel.readJson();
```
```ballerina
// Reading an XML.
var result = readableCharChannel.readXml();
```

```ballerina
// Create a `WritableCharacterChannel` from the `WritableByteChannel`.
var writableCharChannel = new io:WritableCharacterChannel(writableByteChannel, "UTF-8");
```

```ballerina
// Writing a JSON.
json content = {fname:"Jhon", lname:"Doe", age:40};
var writeResult = writableCharChannel.writeJson(content);
if (writeResult is error) {
    return writeResult;
} else {
    io:println("JSON content written successfully.");
}
```

### Record channels
Ballerina also supports I/O for delimited records.

```ballerina
// Create a `ReadableTextRecordChannel` from the `ReadableCharacterChannel`.
// Records are separated using a new line, and fields of a record are separated using a comma.
var readableRecordsChannel = new io:ReadableTextRecordChannel(readableCharChannel, fs = ",", rs = "\n");

// Read few records.
while (readableRecordsChannel.hasNext()) {
    var result = readableRecordsChannel.getNext();
    if (result is string[]) {
        io:println(result); // Retrieved a record.
    } else {
        return result; // An IO error occurred when reading the records.
    }
}
```

A `.CSV` file can be read and written directly into a `CSVChannel`, as shown in this code snippet.

```ballerina
// Create a `ReadableCSVChannel` from the `ReadableCharacterChannel`.
var readableCSVChannel = new io:ReadableCSVChannel(readableCharChannel);
```

Records of the `.CSV` file can read directly into a table of a matching type.

 ```ballerina
// First let’s define a type that matches a record in the CSV file.
type Employee record {
    string id;
    string name;
    float salary;
};

// Now read the CSV file as a table of Employees and compute total salary.
float total = 0.0;
var tableResult = csv.getTable(Employee);
if (tableResult is table<Employee>) {
     foreach var x in tableResult {
       total = total + x.salary;
     }
     return total;
} else if (tableResult is error) {
     return tableResult; //Return the error back to the caller
} else {
     error e = error(IO_ERROR_CODE, { message : "Record channel not initialized properly" });
     return e;
}
```

### Data Channels
Ballerina supports performing data i/o operations

Person object could be serialized into a file or a network socket in the following manner.

```ballerina
public type Person record {
    string name;
    int age;
    float income;
    boolean isMarried;
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

// Deserialize record into binary.
function deserialize(io:ReadableByteChannel byteChannel) returns Person {
    Person person = {};
    int nameLength = 0;
    string nameValue;
    io:ReadableDataChannel dc = new io:ReadableDataChannel(byteChannel);
    // Read 32 bit signed integer.
    var int32Result = dc.readInt32();
    if (int32Result is int) {
        nameLength = int32Result;
    } else if (int32Result is error) {
        log:printError("Error occurred while reading name length", err = int32Result);
    }
    // Read UTF-8 encoded string represented through specified amount of bytes.
    var strResult = dc.readString(nameLength, "UTF-8");
    if (strResult is string) {
        person.name = strResult;
    } else if (strResult is error) {
        log:printError("Error occurred while reading name", err = strResult);
    }
    // Read 16 bit signed integer.
    var int16Result = dc.readInt16();
    if (int16Result is int) {
        person.age = int16Result;
    } else if (int16Result is error) {
        log:printError("Error occurred while reading age", err = int16Result);
    }
    // Read 64 bit signed float.
    var float64Result = dc.readFloat64();
    if (float64Result is float) {
        person.income = float64Result;
    } else if (float64Result is error) {
        log:printError("Error occurred while reading income", err = float64Result);
    }
    // Read boolean.
    var boolResult = dc.readBool();
    if (boolResult is boolean) {
        person.isMarried = boolResult;
    } else if (boolResult is error) {
        log:printError("Error occurred while reading marital status", err = boolResult);
    }
    // Finally close the data channel.
    var closeResult = dc.close();
    return person;
}
```
