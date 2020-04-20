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

import ballerina/java;

# Represents a record type to provide configurations for the JSON to XML
# conversion.
#
# + attributePrefix - The attribute prefix to use in the XML representation
# + arrayEntryTag - The XML tag to add an element from a JSON array
public type JsonOptions record {
    string attributePrefix = "@";
    string arrayEntryTag = "root";
};

# Converts a JSON object to an XML representation.
# ```ballerina
# json data = {
#     name: "John",
#     age: 30
# };
# xml|error xmlValue = xmlutils:fromJSON(data);
# ```
#
# + jsonValue - The JSON source
# + options - The `xmlutils:JsonOptions` record for JSON to XML conversion properties
# + return - XML representation of the given JSON if the JSON is
#            successfully converted or else an `error`
public function fromJSON(json? jsonValue, JsonOptions options = {}) returns xml|error {
    return externFromJson(jsonValue, options);
}

// This is a temporary fix until resolve the #19917
function externFromJson(json? jsonValue, JsonOptions options = {}) returns xml|error = @java:Method {
    name: "fromJSON",
    class: "org.ballerinalang.stdlib.xmlutils.ConvertUtils"
} external;


//TODO Table remove - Fix
//# Converts a table to its XML representation.
//# ```ballerina
//# table<Person> personTable = table{
//#     { key id, age, salary, name, married },
//#     [ { 1, 30,  300.5, "Mary", true },
//#         { 2, 20,  300.5, "John", true }
//#     ]
//# };
//# xml xmlValue = xmlutils:fromTable(personTable).toString();
//# ```
//#
//# + tableValue - The `table` value to be converted to an XML
//# + return - The XML representation of the provided table
//public function fromTable(table<record{}> tableValue) returns xml {
//    return externFromTable(tbl);
//}
//
//function externFromTable(table<record{}> tableValue) returns xml = @java:Method {
//    name: "fromTable",
//    class: "org.ballerinalang.stdlib.xmlutils.ConvertUtils"
//} external;
