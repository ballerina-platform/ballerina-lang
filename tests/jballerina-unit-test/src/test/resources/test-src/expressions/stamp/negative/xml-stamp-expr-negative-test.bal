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

type BookRecord record {
   string book = "ABC";
};

function stampXMLToRecord() returns BookRecord|error {

    xml xmlValue = xml `<book>The Lost World</book>`;

    BookRecord|error recordValue = xmlValue.cloneWithType(BookRecord);
    return recordValue;
}

function stampXMLToJson() returns json|error {

    xml xmlValue = xml `<book>The Lost World</book>`;

    json|error jsonValue = xmlValue.cloneWithType(json);
    return jsonValue;
}

function stampXMLToMap() returns map<anydata>|error {

    xml xmlValue = xml `<book>The Lost World</book>`;

    map<anydata>|error mapValue = xmlValue.cloneWithType(map<anydata>);
    return mapValue;
}

function stampXMLToArray() returns BookRecord[]|error {

    xml xmlValue = xml `<book>The Lost World</book>`;

    BookRecord[]|error arrayValue = xmlValue.cloneWithType(BookRecord[]);
    return arrayValue;
}

function stampXMLToTuple() returns [string, string]|error {

    xml xmlValue = xml `<book>The Lost World</book>`;

    [string, string]|error tupleValue = xmlValue.cloneWithType([string, string]);
    return tupleValue;
}
