// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

string returnValue;
io:ServerSocket server;

function getResultValue() returns string {
    return returnValue;
}

function readAllCharacters(io:ReadableCharacterChannel characterChannel) returns string|error? {
    int fixedSize = 50;
    boolean isDone = false;
    string result;
    while (!isDone) {
        match readCharacters(fixedSize, characterChannel) {
            string value => {
                result = result + value;
            }
            error err => {
                if (err.message == "io.EOF"){
                    isDone = true;
                } else {
                    return err;
                }
            }
        }
    }
    return result;
}

function readCharacters(int numberOfCharacters, io:ReadableCharacterChannel characterChannel) returns string|error {
    var result = characterChannel.read(numberOfCharacters);
    match result {
        string characters => {
            return characters;
        }
        error err => {
            return err;
        }
    }
}

function initServer(int port) {
    server = new();
    check server.bindAddress(port);
    io:println("Server started");
}

function startServerSocket(string welcomeMsg) {
    match server.accept() {
        io:Socket s => {
            io:println("Client socket accepted!!!");
            io:println(s.remotePort);
            io:ReadableByteChannel rch = s.readableChannel;
            io:WritableByteChannel wch = s.writableChannel;
            byte[] c1 = welcomeMsg.toByteArray("utf-8");
            match wch.write(c1, 0) {
                int i => {
                    io:println("No of bytes written: ", i);
                }
                error e2 => {
                    io:println("Channel write error: ", e2.message);
                }
            }
            io:ReadableCharacterChannel? characterChannel1 = new io:ReadableCharacterChannel(rch, "utf-8");
            match characterChannel1 {
                io:ReadableCharacterChannel characterChannel => {
                    match readAllCharacters(characterChannel) {
                        string str => {
                            returnValue = untaint str;
                        }
                        error err => {
                            io:println("Error in read: ", err.message);
                        }
                        () => {
                            io:println("Empty return from channel.");
                        }
                    }
                    match characterChannel.close() {
                        error e1 => {
                            io:println("CharacterChannel close error: ", e1.message);
                        }
                        () => {
                            io:println("Connection closed successfully.");
                        }
                    }
                }
                () => {

                }
            }
            check s.close();
            io:println("Client done.");
        }
        error e10 => {
            io:println("Socket accept error: ", e10.message);
        }
    }
    check server.close();
}
