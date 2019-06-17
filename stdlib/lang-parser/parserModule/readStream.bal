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

public function main() {
    PackageNode? parsedFile = parseFile("inputFile.bal");
    if (parsedFile is PackageNode) {
        // json|error j = json.convert(parsedFile);
        //    if (j is json) {
        //        io:println(j);
        //    }
        io:println(parsedFile);
    }
}

//function to parse the input file, build the AST and return the PackageNode
function parseFile(string fileLocation) returns PackageNode? {
    io:ReadableCharacterChannel sourceChannel = new(io:openReadableFile(fileLocation), "UTF-8");

    BufferReader | error bReader = trap new BufferReader(capacity = 5, sourceChannel);

    if (bReader is error) {
        log:printError("error occurred while processing chars ", err = bReader);
        closeRc(sourceChannel);

        return ();
    } else {
        Lexer lex = new(bReader);
        ParserBufferReader pBuffer = new(lex, capacity = 5);
        Parser parser = new(pBuffer);
        PackageNode pkgNode = parser.parse();
        closeRc(sourceChannel);

        return pkgNode;
    }
}

function closeRc(io:ReadableCharacterChannel ch) {
    var cr = ch.close();
    if (cr is error) {
        log:printError("Error occured while closing the channel: ", err = cr);
    }
}
