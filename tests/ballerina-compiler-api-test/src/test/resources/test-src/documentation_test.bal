// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# This is a record
# + foo - Field foo
# + bar - Field bar
type Annot record {
    # Field foo
    string foo;
    # Field bar
    int bar?;
};

# This is an annotation
public const annotation Annot v1 on source type, class, service, annotation, var, const, worker;

# This is a class
class Person {
    # Field name
    string name;

    # Method init
    # + name - Param name
    # + return - error or nil
    function init(string name) returns error? {
        self.name = name;
    }

    # Method getName
    # + return - string
    function getName() returns string => self.name;
}

# This is a constant
public const PI = 3.14;

# This is an enum
public enum Colour {
    # Enum member RED
    RED,
    # Enum member GREEN
    GREEN,
    # Enum member BLUE
    BLUE
}

# This is a function
# + x - Param x
# + y - Param y
# + return - The sum
function sum(int x, int y) returns int => x + y;

# This is a variable
string greet = "Hello";
