// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

string output = "";

function stringConcat(string value){
    output = output + value + " ";
}

function testMapWithArityOne () returns (string) | error {
    map<any> m = {a:"1A", b:"2B", c:"3C", d:"4D"};
    output = "";
    string val;
    foreach var v in m {
        val = <string> v;
        stringConcat(val);
    }
    return output;
}

function testMapWithArityTwo () returns (string) | error {
    map<any> m = {a:"1A", b:"2B", c:"3C", d:"4D"};
    output = "";
    string val;
    foreach var v in m {
        val = <string> v;
        stringConcat(val);
    }
    return output;
}

function testDeleteWhileIteration () returns (string) | error {
    map<any> m = {a:"1A", b:"2B", c:"3C"};
    output = "";
    string val;
    foreach var v1 in m.keys() {
        if (v1 == "a") {
            _ = m.remove("c");
            foreach var v2 in m {
                val = <string> v2;
                stringConcat(val);
            }
        }
    }
    return output;
}

function testAddWhileIteration () returns (string) | error {
    map<any> m = {a:"1A", b:"2B", c:"3C"};
    output = "";
    string val1;
    string val2;
    foreach var v1 in m {
		val1 = <string> v1;
        stringConcat(val1);
        m[val1 + val1] = val1 + val1;
        foreach var v2 in m {
            val2 = <string> v2;
            stringConcat(val2);
        }
        output = output + "\n";
    }
    return output;
}
