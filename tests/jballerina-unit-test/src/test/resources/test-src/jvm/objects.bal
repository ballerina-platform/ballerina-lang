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

public function testSimpleObjectAsStruct () returns [int, string, int, string] {
    Person p = new Person();
    return [p.age, p.getName(), p.getYear(), p.month];
}

public function testSimpleObjectAsStructWithNew () returns [int, string, int, string] {
    Person p = new;
    return [p.age, p.getName(), p.getYear(), p.month];
}

class Person {
    public int age = 10;
    public string name = "sample name";

    int year = 50;
    string month = "february";

    function getName() returns string {
        return self.name;
    }

    function getYear() returns int {
        return self.getYearInternal();
    }

    function getYearInternal() returns int {
        return self.year;
    }
}

public function testUserInitFunction() returns [int, string, int, string] {
    Person2 p = new(10, "sample name");
    return [p.age, p.name, p.year, p.month];
}

class Person2 {
    public int age = 3;
    public string name = "no name";

    int year = 2;
    string month = "idk";

    function init(int age, string name) {
        self.age = age;
        self.name = name;
        self.year = 50;
        self.month = "february";
    }
}

public class Foo {
    public int a = 3;
    public Foo? f = ();
}

public function testSelfReferencingObject() returns Foo {
    return new Foo();
}


class objects {
    function getName() returns string {
        return "works!";
    }
}

function testObjectWithSameNameAsFileName() returns string {
    objects o = new();
    return o.getName();
}
