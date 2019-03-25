import ballerina/io;
import ballerina/log;

public type BufferReader object {
    private int capacity;
    private int readChr = 0;
    private io:ReadableCharacterChannel sourceChannel2;
    private string[] characterArray = [];
    boolean isEof = false;
    private int bufferSize = 0;

    public function __init(int capacity = 0, io:ReadableCharacterChannel sourceChannel2) {
        self.capacity = capacity;
        self.sourceChannel2 = sourceChannel2;
        var result = self.fillBuffer();
        if (result is error) {
            // Doing this right now as we cannot handle errors returned from constructor
            panic result;
        }
    }

    function fillBuffer() returns error? {
        while (self.bufferSize < self.capacity) {
            self.characterArray[self.bufferSize] = check self.sourceChannel2.read(1);
            if (self.characterArray[self.bufferSize] == "") {
                break;
            }
            self.bufferSize = self.bufferSize + 1;
        }
    }

    function printBuffer() {
        foreach var item in self.characterArray {
            io:println(item);
        }
    }

    function isEOF() returns boolean {
        boolean checker = false;
        if (self.characterArray[self.readChr] == "") {
            checker = true;
        }
        return checker;
    }

    function consume() returns string {
        string returnCharacter = self.characterArray[self.readChr];
        if (!self.isEof && self.bufferSize == self.capacity) {
            var storeResult = self.storeCharacter();
            if (storeResult is error) {
                log:printError("error occurred while processing chars ", err = storeResult);
            }
        }
        self.readChr = (self.readChr + 1) % self.capacity;
        return returnCharacter;
    }

    function storeCharacter() returns error? {
        self.characterArray[self.readChr] = check self.sourceChannel2.read(1);
        if (self.characterArray[self.readChr] == "") {
            self.isEof = true;
        }

    }
    function lookAhead(int lookAheadCount = 1) returns string {
        int a = (self.readChr - 1 + lookAheadCount) % self.capacity;
        return self.characterArray[a];
    }
};