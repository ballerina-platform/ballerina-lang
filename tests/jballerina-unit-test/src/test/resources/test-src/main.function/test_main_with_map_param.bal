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
import ballerina/io;

public type Employee record {
   string name;
};

public function main(map<int> intMap, map<string> strMap, map<float> floatMap, map<boolean> boolMap, map<json> jsonMap,
                     map<Employee> empMap) {
    io:print("integer: " + intMap.get("test").toString() + ", string: " + strMap.get("test") + ", float: " +
            floatMap.get("test").toString()  + ", boolean: " + boolMap.get("test").toString() + ", json: " +
            jsonMap.get("test").toString() + ", Test Employee Name Field: " + empMap.get("test").name);
}
