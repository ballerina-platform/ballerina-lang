import ballerina/test;
import ballerina/io;

any[] outputs = [];
int counter = 0;

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina/io",
    functionName: "println"
}
public function mockPrint(any... s) {
    outputs[counter] = s[0];
    counter += 1;
}

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina-examples/record-io:0.0.1",
    functionName: "getReadableRecordChannel"
}
public function mockGetReadableRecordChannel(string filePath, string encoding,
                                             string rs, string fs)
                    returns io:ReadableTextRecordChannel {
    string path = "./record-io/" + filePath;
    io:ReadableByteChannel byteChannel = io:openReadableFile(path);
    io:ReadableCharacterChannel characterChannel = new(byteChannel, encoding);
    io:ReadableTextRecordChannel delimitedRecordChannel = new(characterChannel,
        rs = rs,
        fs = fs);
    return delimitedRecordChannel;
}

// This is the mock function that replaces the real function.
@test:Mock {
    moduleName: "ballerina-examples/record-io:0.0.1",
    functionName: "getWritableRecordChannel"
}
public function mockGetWritableRecordChannel(string filePath, string encoding,
                                             string rs, string fs)
                    returns io:WritableTextRecordChannel {
    string path = "./record-io/" + filePath;
    io:WritableByteChannel byteChannel = io:openWritableFile(path);
    io:WritableCharacterChannel characterChannel = new(byteChannel, encoding);
    io:WritableTextRecordChannel delimitedRecordChannel = new(characterChannel,
        rs = rs,
        fs = fs);
    return delimitedRecordChannel;
}

@test:Config
function testFunc() {
    // Invoke the main function.
    main();
    string out1 = "Start processing the CSV file from ./files/sample.csv to the text file in";
    string out2 = "Processing completed. The processed file is located in";
    string actual1 = <string>outputs[0];
    string actual2 = <string>outputs[1];
    test:assertEquals(outputs.length(), 2);
    test:assertTrue(actual1.contains(out1));
    test:assertTrue(actual2.contains(out2));
}
