/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import ballerina/io;
import ballerina/log;

function closeRc(io:ReadableCharacterChannel ch) {
    var cr = ch.close();
    if (cr is error) {
        log:printError("Error occurred while closing the channel: ", err = cr);
    }
}

function close(io:ReadableByteChannel|io:WritableByteChannel ch) {
    abstract object {
        public function close() returns error?;
    } channelResult = ch;
    var cr = channelResult.close();
    if (cr is error) {
        log:printError("Error occurred while closing the channel: ", cr);
    }
}

public function main() returns @tainted error? {
    io:ReadableByteChannel readableFieldResult = check io:getResource("abc/pqr/resource.txt");
    io:ReadableCharacterChannel sourceChannel = new(readableFieldResult, "UTF-8");
    io:println(check sourceChannel.read(1000));
    closeRc(sourceChannel);
    close(readableFieldResult);

    string[] readDirResults = io:getResources();
    foreach var item in readDirResults {
        io:println(item);
    }
}
