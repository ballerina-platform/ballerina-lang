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

type BookObject object {
    string book = "XYZ";
};

function stampXMLToRecord() returns BookRecord {

    xml xmlValue = xml `<book>The Lost World</book>`;

    BookRecord recordValue = BookRecord.stamp(xmlValue);
    return recordValue;
}

function stampJSONToRecord() returns json {

    xml xmlValue = xml `<book>The Lost World</book>`;

    json jsonValue = json.stamp(xmlValue);
    return jsonValue;
}

function stampXMLToObject() returns BookObject {

    xml xmlValue = xml `<book>The Lost World</book>`;

    BookObject objectValue = BookObject.stamp(xmlValue);
    return objectValue;
}

function stampXMLToMap() returns map {

    xml xmlValue = xml `<book>The Lost World</book>`;

    map mapValue = map.stamp(xmlValue);
    return mapValue;
}

function stampXMLToArray() returns BookRecord[] {

    xml xmlValue = xml `<book>The Lost World</book>`;

    BookRecord[] arrayValue = BookRecord[].stamp(xmlValue);
    return arrayValue;
}

function stampXMLToTuple() returns (string, string) {

    xml xmlValue = xml `<book>The Lost World</book>`;

    (string, string) tupleValue = (string, string).stamp(xmlValue);
    return tupleValue;
}
