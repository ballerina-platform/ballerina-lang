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

import configRecordArray.type_defs;
import testOrg/configLib.util;
import ballerina/test;

configurable type_defs:Person[] & readonly personArray = ?;
configurable (type_defs:Person & readonly)[] & readonly personArray1 = ?;
configurable type_defs:PersonArray & readonly personArray2 = ?;

public function testArrays() {
    test:assertEquals(personArray.toString(), "[{\"name\":\"waruna\",\"id\":111,\"address\":{\"city\":\"Abu Dhabi\"," +
    "\"country\":{\"name\":\"UAE\"}}},{\"name\":\"manu\",\"id\":122,\"address\":{\"city\":\"Mumbai\"," +
    "\"country\":{\"name\":\"India\"}}}]");
    test:assertEquals(personArray1.toString(), "[{\"name\":\"manu\",\"id\":800,\"address\":{\"city\":\"New York\"," +
    "\"country\":{\"name\":\"USA\"}}},{\"name\":\"hinduja\",\"id\":801,\"address\":{\"city\":\"London\"," +
    "\"country\":{\"name\":\"UK\"}}}]");
    test:assertEquals(personArray2.toString(), "[{\"name\":\"riyafa\",\"id\":1000,\"address\":{\"city\":\"New York\"," +
    "\"country\":{\"name\":\"USA\"}}},{\"name\":\"waruna\",\"id\":1001,\"address\":{\"city\":\"London\"," +
    "\"country\":{\"name\":\"UK\"}}}]");
}

public function testArrayIteration() {
    util:testArrayIterator(personArray, 2);
    util:testArrayIterator(personArray1, 2);
    util:testArrayIterator(personArray2, 2);    
}
