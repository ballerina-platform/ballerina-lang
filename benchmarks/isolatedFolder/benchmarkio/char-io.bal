package benchmarkio;

import ballerina/io;

public function benchmarkinitCharacterChannelReadMode() {
    io:ByteChannel channel = io:openFile("isolatedFolder/benchmarkio/resources/test.txt", "r");
    var result = io:createCharacterChannel(channel, "UTF-8");
    io:CharacterChannel characterChannel;
    match result {
        io:CharacterChannel charChannel => {
            characterChannel = charChannel;
        }
        io:IOError err => {}
    }
    var err = characterChannel.closeCharacterChannel();
    var results = channel.close();
}

public function benchmarkinitCharacterChannelWriteMode() {
    io:ByteChannel channel = io:openFile("isolatedFolder/benchmarkio/resources/test.txt", "w");
    var result = io:createCharacterChannel(channel, "UTF-8");
    io:CharacterChannel characterChannel;
    match result {
        io:CharacterChannel charChannel => {
            characterChannel = charChannel;
        }
        io:IOError err => {}
    }
    var err = characterChannel.closeCharacterChannel();
    var results = channel.close();
}

public function benchmarkinitCharacterChannelAppendMode() {
    io:ByteChannel channel = io:openFile("isolatedFolder/benchmarkio/resources/test.txt", "a");
    var result = io:createCharacterChannel(channel, "UTF-8");
    io:CharacterChannel characterChannel;
    match result {
        io:CharacterChannel charChannel => {
            characterChannel = charChannel;
        }
        io:IOError err => {}
    }
    var err = characterChannel.closeCharacterChannel();
    var results = channel.close();
}

public function benchmarkReadCharecters() {
    io:ByteChannel channel = io:openFile("isolatedFolder/benchmarkio/resources/test.txt", "r");
    var result = io:createCharacterChannel(channel, "UTF-8");
    io:CharacterChannel characterChannel;
    match result {
        io:CharacterChannel charChannel => {
            characterChannel = charChannel;
        }
        io:IOError err => {}
    }
    var resultR = characterChannel.readCharacters(5);
    var err = characterChannel.closeCharacterChannel();
    var results = channel.close();
}

public function benchmarkWriteCharecters() {
    io:ByteChannel channel = io:openFile("isolatedFolder/benchmarkio/resources/test.txt", "w");
    var result = io:createCharacterChannel(channel, "UTF-8");
    io:CharacterChannel characterChannel;
    match result {
        io:CharacterChannel charChannel => {
            characterChannel = charChannel;
        }
        io:IOError err => {}
    }
    var resultW = characterChannel.writeCharacters("This file is used for io testing.", 0);
    var err = characterChannel.closeCharacterChannel();
    var results = channel.close();
}

public function benchmarkReadJson() {
    io:ByteChannel channel = io:openFile("isolatedFolder/benchmarkio/resources/testJson.txt", "r");
    var result = io:createCharacterChannel(channel, "UTF-8");
    io:CharacterChannel characterChannel;
    match result {
        io:CharacterChannel charChannel => {
            characterChannel = charChannel;
        }
        io:IOError err => {}
    }
    var resultR = characterChannel.readJson();
    var err = characterChannel.closeCharacterChannel();
    var results = channel.close();
}

public function benchmarkWriteJson() {
    io:ByteChannel channel = io:openFile("isolatedFolder/benchmarkio/resources/testJson.txt", "w");
    var result = io:createCharacterChannel(channel, "UTF-8");
    io:CharacterChannel characterChannel;
    match result {
        io:CharacterChannel charChannel => {
            characterChannel = charChannel;
        }
        io:IOError err => {}
    }
    json content = {"Hello":"world"};
    var resultW = characterChannel.writeJson(content);
    var err = characterChannel.closeCharacterChannel();
    var results = channel.close();
}

public function benchmarkReadXML() {
    io:ByteChannel channel = io:openFile("isolatedFolder/benchmarkio/resources/testXML.txt", "r");
    var result = io:createCharacterChannel(channel, "UTF-8");
    io:CharacterChannel characterChannel;
    match result {
        io:CharacterChannel charChannel => {
            characterChannel = charChannel;
        }
        io:IOError err => {}
    }
    var resultR = characterChannel.readXml();
    var err = characterChannel.closeCharacterChannel();
    var results = channel.close();
}

public function benchmarkWriteXML() {
    io:ByteChannel channel = io:openFile("isolatedFolder/benchmarkio/resources/testXML.txt", "w");
    var result = io:createCharacterChannel(channel, "UTF-8");
    io:CharacterChannel characterChannel;
    match result {
        io:CharacterChannel charChannel => {
            characterChannel = charChannel;
        }
        io:IOError err => {}
    }
    xml content = xml `<test>XML</test>`;
    var resultW = characterChannel.writeXml(content);
    var err = characterChannel.closeCharacterChannel();
    var results = channel.close();
}

