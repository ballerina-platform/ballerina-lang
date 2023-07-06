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

class World {
    public string name = "Earth";
    public string value = "Happy 🙋";
}

function testObjectAccess() returns int {
    World world = new;
    string content = world.name + world.value;
    return content.length();
}

class Criminal {

    public string name1;
    public string name2;

    function init(string name1, string name2) {
        self.name1 = name1;
        self.name2 = validate(name2);
    }

    function validate(string name) returns string {
        return name;
    }

    function getName() returns string {
        return validate(self.name2);
    }
}

function validate(string name) returns string {
    return name;
}

function testObjectInitialization() returns int {
    Criminal criminal = new Criminal("John", "Doe🦹‍♂️");
    string content = criminal.validate(criminal.name1) + criminal.getName();
    return content.toString().length();
}

function testObjectSet() returns int {
    World world = new();
    world.name = "Sam";
    world.value = "Andy👨";
    string content = world.name + world.value;
    return content.length();
}

