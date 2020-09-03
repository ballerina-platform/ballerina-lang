// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

public type Address object {
    public string city;

    public function getCity() returns string;
};

public class StudentAddress {
    public string city;

    public function init(string city){
        self.city = city;
    }

    public function getCity() returns string {
        return self.city;
    }
}

public class Employee {
    public string city;
    public Address address;

    function init(string city, Address address) {
        self.city = city;
        self.address = address;
    }

    public function getCity() returns string {
        return self.city;

    }
}

public function testAbstractObjectInObject() returns Employee {
    // Initializing variable of object type Person
    StudentAddress stdAddr = new("Colombo");
    Employee emp = new("Colombo", stdAddr);
    return emp;
}
