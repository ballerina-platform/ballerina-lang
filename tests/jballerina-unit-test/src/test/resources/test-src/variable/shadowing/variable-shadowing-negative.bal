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

xmlns "http://sample.com/wso2/a1" as ns;

string name = "";

function printName(any name) {

    xmlns "http://sample.com/wso2/a2" as ns;
    var name = formatName(name);

    if (<string>name == ""){
        xmlns "http://sample.com/wso2/a3" as ns;
        var getName = function () returns (string) {
            string name = "Ballerina";
            return "";
        };
        name = getName.call();
    }

    xmlns "http://sample.com/wso2/a3" as ns;
}

function formatName(any a) returns any {
    xmlns "http://sample.com/wso2/a3" as ns;
    return a;
}

type User object {

    public string name = "";


    function f(string name) {
        xmlns "http://sample.com/wso2/a4" as ns;
        string name = "";
        xmlns "http://sample.com/wso2/a4" as ns;
    }
};

service ser = service {

    string name = "";

    resource function res(string name) {
        xmlns "http://sample.com/wso2/a6" as ns;
        string name = "";
        xmlns "http://sample.com/wso2/a7" as ns;
    }
};
