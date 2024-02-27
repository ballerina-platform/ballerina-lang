// Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

# Represents a person object.
#
# + name - Name of the person
# + age - Age of the person in years
# + address - Address of the person
# + wealth - Account balance of the person
public class Person {
    public string name = "";
    public int age=5;
    public string address = "";
    public float wealth = 0;

    # Gets invoked to initialize the `Person` object.
    #
    # + name - Name of the person for the constructor
    # + age - Age of the person for the constructor
    public function init(string name, int age) {
    }

    # Get the address of the person.
    #
    # + return - New address of the person
    public function getAddress() returns string {
        return self.address ;
    }

    # Add wealth of the person.
    #
    # + amt - Amount to be added
    # + rate - Interest rate
    public function addWealth(int[] amt, float rate=1.5) {
    }
}
