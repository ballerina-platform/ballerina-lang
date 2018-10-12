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
import ballerina/internal;
import ballerina/system;

public type PIIFileFormat "CSV"|"TDF";

@final public PIIFileFormat CSV = "CSV";
@final public PIIFileFormat TDF = "TDF";

public type FilePIIStore object {
    string path;
    PIIFileFormat fileFormat;
    string encoding;

    public new(path, fileFormat, encoding = "UTF-8") {

    }

    function pseudonymize (string pii) returns string|error {
        string[] pseudoRecord = [system:uuid(), pii];
        if (fileFormat == TDF) {
            return writeDelimitedRecord(path, encoding, "TDF", pseudoRecord);
        } else {
            return writeDelimitedRecord(path, encoding, "CSV", pseudoRecord);
        }
    }

    function depseudonymize (string id) returns string|error {
        if (fileFormat == TDF) {
            return searchDelimitedRecord(path, encoding, "TDF", id);
        } else {
            return searchDelimitedRecord(path, encoding, "CSV", id);
        }
    }

    function delete (string id) returns ()|error {
        if (fileFormat == TDF) {
            return deleteDelimitedRecord(path, encoding, "TDF", id);
        } else {
            return deleteDelimitedRecord(path, encoding, "CSV", id);
        }
    }
};

function writeDelimitedRecord(string filePath, string encoding, string fmt, string[] pseudoRecord) returns string|error {
    io:ByteChannel byteChannel = io:openFile(filePath, io:APPEND);
    io:CharacterChannel characterChannel = new(byteChannel, encoding);
    io:DelimitedTextRecordChannel delimitedRecordChannel = new(characterChannel, rs = fmt);
    
    var writeResult = delimitedRecordChannel.write(pseudoRecord);
    var closeResult = delimitedRecordChannel.close();
    match writeResult {
        error err => {
            return err;
        }
        () => {
            return pseudoRecord[0];
        }
    }
    return "";
}

function searchDelimitedRecord(string filePath, string encoding, string fmt, string id) returns string|error {
    io:ByteChannel byteChannel = io:openFile(filePath, io:READ);
    io:CharacterChannel characterChannel = new(byteChannel, encoding);
    io:DelimitedTextRecordChannel delimitedRecordChannel = new(characterChannel, rs = fmt);

    while(delimitedRecordChannel.hasNext()) {
        var readResult = delimitedRecordChannel.getNext();
        match readResult {
            string[] pseudoRecord => {
                if (pseudoRecord[0] == id) {
                    return pseudoRecord[1];
                }           
            }
            error err => {
                return err; 
            }
        }
    }

    var closeResult = delimitedRecordChannel.close();
    error notFound = { message: "Identifier not found in PII store" };
    return notFound;
}

function deleteDelimitedRecord(string filePath, string encoding, string fmt, string id) returns error? {
    var searchResult = searchDelimitedRecord(filePath, encoding, fmt, id);
    match searchResult {
        string => {
            io:ByteChannel byteChannel = io:openFile(filePath, io:READ);
            io:CharacterChannel characterChannel = new(byteChannel, encoding);
            io:DelimitedTextRecordChannel delimitedRecordChannel = new(characterChannel, rs = fmt);

            io:ByteChannel tempFileByteChannel = io:openFile(filePath + ".temp", io:WRITE);
            io:CharacterChannel tempFileCharacterChannel = new(tempFileByteChannel, encoding);
            io:DelimitedTextRecordChannel tempFileDelimitedRecordChannel = new(tempFileCharacterChannel, rs = fmt);

            while(delimitedRecordChannel.hasNext()) {
                var readResult = delimitedRecordChannel.getNext();
                match readResult {
                    string[] pseudoRecord => {
                        if (pseudoRecord[0] != id) {
                            var writeResult = tempFileDelimitedRecordChannel.write(pseudoRecord);
                            match writeResult {
                                error err => {
                                    return err;
                                }
                                () => {
                                    // Do nothing
                                }
                            }
                        }           
                    }
                    error err => {
                        return err; 
                    }
                }
            }
            var closeResult = delimitedRecordChannel.close();
            var tempFileCloseResult = tempFileDelimitedRecordChannel.close();

            internal:Path path = new(filePath);
            var deleteResult = path.delete();
            match deleteResult {
                error err => {
                    return err;
                }
                () => {
                    internal:Path tempFilePath = new(filePath + ".temp");
                    var moveResult = tempFilePath.moveTo(path);

                    match moveResult {
                        error err => {
                            return err;
                        }
                        () => {
                            return;
                        }
                    }
                }
            }
        }
        error err => {
            return err; 
        }
    }
}
