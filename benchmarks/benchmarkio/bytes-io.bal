import ballerina/io;

public function benchmarkInitFileChannelReadMode() {
    io:ReadableByteChannel byteChannel;
    byteChannel = io:openReadableFile("benchmarkio/resources/test.txt");
    var result = byteChannel.close();
}

public function benchmarkInitFileChannelWriteMode() {
    io:WritableByteChannel byteChannel;
    byteChannel = io:openWritableFile("benchmarkio/resources/test.txt");
    var result = byteChannel.close();
}

public function benchmarkReadBytes() {
    io:ReadableByteChannel byteChannel;
    byteChannel = io:openReadableFile("benchmarkio/resources/test.txt");
    var result = byteChannel.read(5);
    var results = byteChannel.close();
}

public function benchmarkWriteBytes() {
    io:WritableByteChannel byteChannel;
    byteChannel = io:openWritableFile("benchmarkio/resources/test.txt");
    string text = "This file is used for io testing.";
    byte[] content = text.toByteArray("UTF-8");
    var result = byteChannel.write(content, 0);
    var results = byteChannel.close();
}

