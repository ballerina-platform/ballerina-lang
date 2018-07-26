// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

documentation {
    Removes each element that matches the given key.

    P{{key}} Key of the field to remove
}
public extern function json::remove(string key);

documentation {
    Converts a JSON object to a string representation.

    R{{}} String value of the converted JSON
}
public extern function json::toString() returns (string);

documentation {
    Returns an array of keys contained in the specified JSON.

    R{{}} A string array of keys contained in the specified JSON
}
public extern function json::getKeys() returns (string[]);

documentation {
    Converts a JSON object to a XML representation.

    P{{options}} jsonOptions struct for JSON to XML conversion properties
    R{{}} The XML representation of the JSON
}
public extern function json::toXML(record {
                                         string attributePrefix = "@",
                                         string arrayEntryTag = "item",
                                     } options) returns (xml|error);
