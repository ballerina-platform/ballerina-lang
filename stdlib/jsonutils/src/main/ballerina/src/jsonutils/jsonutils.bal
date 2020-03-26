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

# Type for XML options.
#
# + attributePrefix - Attribute prefix used in the XML.
# + preserveNamespaces - Instructs whether to preserve the namespaces of the XML when converting.
public type XmlOptions record {
    string attributePrefix = "@";
    boolean preserveNamespaces = true;
};

# Converts an XML object to its JSON representation.
#
# + x - The xml source
# + options - XmlOptions record for XML to JSON conversion properties
# + return - JSON representation of the given XML, or an error if the conversion fails
public function fromXML(xml x, XmlOptions options = {}) returns json|error {
    json|error|handle result = externFromXML(x, options);
    if (result is handle) {
        return java:toString(result);
    } else {
        return result;
    }
}

function externFromXML(xml x, XmlOptions options = {}) returns json|error|handle = @java:Method {
    name: "fromXML",
    class: "org.ballerinalang.stdlib.jsonutils.FromXML"
} external;

# Converts a table to its json representation.
#
# + tbl - The source table
# + return - JSON representation of source table
public function fromTable(table<record{}> tbl) returns json {
    return externFromTable(tbl);
}

function externFromTable(table<record{}> tbl) returns json = @java:Method {
    name: "fromTable",
    class: "org.ballerinalang.stdlib.jsonutils.FromTable"
} external;
