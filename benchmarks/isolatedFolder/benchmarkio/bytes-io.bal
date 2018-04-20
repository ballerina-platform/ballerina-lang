package benchmarkio;

import ballerina/io;

public function benchmarkInitFileChannelReadMode() {
    io:ByteChannel channel;
    channel = io:openFile("isolatedFolder/benchmarkio/resources/test.txt", "r");
    var result = channel.close();
}

public function benchmarkInitFileChannelWriteMode() {
    io:ByteChannel channel;
    channel = io:openFile("isolatedFolder/benchmarkio/resources/test.txt", "w");
    var result = channel.close();
}

public function benchmarkInitFileChannelAppendMode() {
    io:ByteChannel channel;
    channel = io:openFile("isolatedFolder/benchmarkio/resources/test.txt", "a");
    var result = channel.close();
}

public function benchmarkReadBytes() {
    io:ByteChannel channel;
    channel = io:openFile("isolatedFolder/benchmarkio/resources/test.txt", "r");
    var result = channel.read(5);
    var results = channel.close();
}

public function benchmarkWriteBytes() {
    io:ByteChannel channel;
    channel = io:openFile("isolatedFolder/benchmarkio/resources/test.txt", "w");
    string text = "This file is used for io testing.";
    blob content = text.toBlob("UTF-8");
    var result = channel.write(content, 0);
    var results = channel.close();
}

