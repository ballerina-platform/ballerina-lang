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

public function main(string? s = (), map<int>? m = ()) {
    string stringToPrint = "string value: ";
    if (s is string) {
        stringToPrint += s;
    } else {
        stringToPrint += "s is nil";
    }

    stringToPrint += " ";

    if (m is map<int>) {
        stringToPrint += io:sprintf("%s", m);
    } else {
        stringToPrint += "m is nil";
    }
    io:print(stringToPrint);
}
