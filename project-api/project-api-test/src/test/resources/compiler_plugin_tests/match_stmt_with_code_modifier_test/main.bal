// Copyright (c) 2023 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import samjs/package_comp_plugin_code_modify_add_function as _;

function listMatchPattern(any v) returns string {
    match v {
        [1] => {
            return "[1]";
        }
        [1, 2] => {
            return "[1, 2]";
        }
    }
    return "No match";
}

isolated function getURI(string nameSpace) returns string {
    match nameSpace {
        xml:XML_NAMESPACE_URI => {
            return xml:XML_NAMESPACE_URI;
        }
        xml:XMLNS_NAMESPACE_URI => {
            return xml:XMLNS_NAMESPACE_URI;
        }
        _ => {
            return "URI not found";
        }
    }
}

public function main() {
    assertEquals("[1]", listMatchPattern([1]));
    assertEquals("[1, 2]", listMatchPattern([1, 2]));
    assertEquals(xml:XML_NAMESPACE_URI, getURI(xml:XML_NAMESPACE_URI));
    assertEquals(xml:XMLNS_NAMESPACE_URI, getURI(xml:XMLNS_NAMESPACE_URI));
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }
    
    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
