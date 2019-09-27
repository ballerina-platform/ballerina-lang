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

# Record to provide configurations for the JSON to XML conversion.
#
# + attributePrefix - attribute prefix to use in XML
# + arrayEntryTag - XML tag to add an element from a JSON array
public type JsonOptions record {
    string attributePrefix = "@";
    string arrayEntryTag = "root";
};

# Converts a JSON object to an XML representation.
#
# + j - The json source
# + options - JsonOptions struct for JSON to XML conversion properties
# + return - XML representation of the given JSON
public function fromJSON(json j, JsonOptions options = {}) returns xml|error = external;

# Converts a table to its xml representation.
#
# + tbl - The source table
# + return - XML representation of source table
public function fromTable(table<record{}> tbl) returns xml = external;
