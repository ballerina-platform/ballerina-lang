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

class Obj {
    int var1 = 12;
}

function nonAnydataTypes() returns string {
    Obj y = new;

    match y {
        {var1: 12} => {return "a";} // pattern will not be matched
        {"var1": 12} => {return "a";} // pattern will not be matched
        {foo(): 12} => {return "a";} // pattern will not be matched and invalid key
    }
    return "Fail";
}

function invalidSimpleVariable() returns string {
    any k = 1;
    string a = "A";
    match k {
        10 => {return "A";} // pattern will always be matched
        a => {return "A";} // invalid literal for match pattern
    }
}