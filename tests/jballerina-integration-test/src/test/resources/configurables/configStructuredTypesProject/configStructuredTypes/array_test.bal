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

import configStructuredTypes.type_defs;
import ballerina/test;

configurable type_defs:Person[] & readonly personArray = ?;
configurable (type_defs:Person & readonly)[] & readonly personArray1 = ?;

type PersonArray type_defs:Person;

configurable PersonArray[] & readonly personArray2 = ?;

public function testArrays() {
    test:assertEquals(personArray.toString(), "[{\"address\":{\"city\":\"New York\",\"country\":" + 
    "{\"name\":\"USA\"}},\"name\":\"manu\",\"id\":11},{\"address\":{\"city\":\"London\",\"country\":" + 
    "{\"name\":\"UK\"}},\"name\":\"hinduja\",\"id\":12}]");
    test:assertEquals(personArray1.toString(), "[{\"address\":{\"city\":\"Abu Dhabi\",\"country\":" + 
    "{\"name\":\"UAE\"}}" + ",\"name\":\"waruna\",\"id\":700},{\"address\":{\"city\":\"Mumbai\"," + 
    "\"country\":{\"name\":\"India\"}},\"name\":\"manu\",\"id\":701}]");
    test:assertEquals(personArray2.toString(), "[{\"address\":{\"city\":\"Abu Dhabi\",\"country\":" + 
    "{\"name\":\"UAE\"}},\"name\":\"gabilan\",\"id\":900},{\"address\":{\"city\":\"Mumbai\"," + 
    "\"country\":{\"name\":\"India\"}},\"name\":\"hinduja\",\"id\":901}]");
}
