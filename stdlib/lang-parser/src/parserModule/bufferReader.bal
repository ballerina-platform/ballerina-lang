// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

import ballerina/io;
import ballerina/log;

const END_OF_FILE = "";

public class BufferReader {
    //size of the character array
    private int capacity;
    //pointer which reads the character array
    private int readPointer = 0;
    private io:ReadableCharacterChannel sourceChannel;
    //array which stores the characters
    private string[] characterArray = [];
    boolean isEof = false;
    //counter to fill the buffer
    private int bufferSize = 0;

    public function init(io:ReadableCharacterChannel sourceChannel, int capacity = 5) returns  @tainted error? {
        self.capacity = capacity;
        self.sourceChannel = sourceChannel;
        return self.fillBuffer();
    }

    # Buffer is filled character by character based on the capacity defined
    #
    # + return - error?
    function fillBuffer() returns error? {
        while (self.bufferSize < self.capacity) {
            self.characterArray[self.bufferSize] = check self.sourceChannel.read(1);
            //if the character is an empty string ,that means it has reached the Eof
            if (self.characterArray[self.bufferSize] == END_OF_FILE) {
                break;
            }
            self.bufferSize = self.bufferSize + 1;
        }
    }

    function isEOF() returns boolean {
        return self.characterArray[self.readPointer] == END_OF_FILE;
    }

    # BufferReader functions as a circular buffer,
    # i:e when one character is consumed, another is filled to the buffer
    #
    # + return - string
    function consume() returns string {
        string currChar = self.characterArray[self.readPointer];
        //if the input file has less number of characters than the buffer size,
        //the buffer size will not be equal to the capacity.
        if (!self.isEof && self.bufferSize == self.capacity) {
            var storeResult = self.storeCharacter();
            if (storeResult is error) {
                log:printError("error occurred while processing chars ", storeResult);
            }
        }
        self.readPointer = (self.readPointer + 1) % self.capacity;
        return currChar;
    }

    # Method to store the characters in buffers for the ones which were consumed.
    #
    # + return - error?
    function storeCharacter() returns error? {
        self.characterArray[self.readPointer] = check self.sourceChannel.read(1);
        if (self.characterArray[self.readPointer] == END_OF_FILE) {
            self.isEof = true;
        }
    }

    # Method to lookahead the characters in the buffer.
    #
    # + lookAheadCount - how many lookaheads do we have to check.
    # + return - string
    function lookAhead(int lookAheadCount = 1) returns string {
        int lookAhead = (self.readPointer - 1 + lookAheadCount) % self.capacity;
        return self.characterArray[lookAhead];
    }
}
