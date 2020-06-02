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



//----------------------------Map Stamp Negative Test Cases-------------------------------------------------------------

function stampMapToXML() returns xml|error {
    map<anydata> m = { "firstName": "mohan", "lastName": "raj" };
    xml|error xmlValue = m.cloneWithType(xml);

    return xmlValue;
}

function stampMapToArray() returns string[]|error {
    map<anydata> m = { "firstName": "mohan", "lastName": "raj" };
    string[]|error arrayValue = m.cloneWithType(string[]);

    return arrayValue;
}

function stampMapToTuple() returns [string,string]|error {
    map<anydata> m = { "firstName": "mohan", "lastName": "raj" };
    [string,string]|error tupleValue = m.cloneWithType([string,string]);

    return tupleValue;
}
