import ballerina/io;

public function benchmarkInitFileChannelReadMode() {
    io:ByteChannel byteChannel;
    byteChannel = io:openFile("benchmarkio/resources/test.txt", "r");
    var result = byteChannel.close();
}

public function benchmarkInitFileChannelWriteMode() {
    io:ByteChannel byteChannel;
    byteChannel = io:openFile("benchmarkio/resources/test.txt", "w");
    var result = byteChannel.close();
}

public function benchmarkInitFileChannelAppendMode() {
    io:ByteChannel byteChannel;
    byteChannel = io:openFile("benchmarkio/resources/test.txt", "a");
    var result = byteChannel.close();
}

public function benchmarkReadBytes() {
    io:ByteChannel byteChannel;
    byteChannel = io:openFile("benchmarkio/resources/test.txt", "r");
    var result = byteChannel.read(5);
    var results = byteChannel.close();
}

public function benchmarkWriteBytes() {
    io:ByteChannel byteChannel;
    byteChannel = io:openFile("benchmarkio/resources/test.txt", "w");
    string text = "This file is used for io testing.";
    byte[] content = text.toByteArray("UTF-8");
    var result = byteChannel.write(content, 0);
    var results = byteChannel.close();
}

