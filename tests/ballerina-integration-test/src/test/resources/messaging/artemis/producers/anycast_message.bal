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

import ballerina/artemis;

artemis:Connection con = new("tcp://localhost:61616");
artemis:Session session = new(con);
artemis:Producer prod = new(session, "anycast_queue", addressConfig = {autoCreated:false});

public function testSendString() {
    var err = prod->send("Hello World");
}

public function testSendByteArray() {
    byte[6] msg = [1, 2, 2, 3, 3, 2];
    var err = prod->send(msg);
}

public function testSendMapString() {
    map<string> msg = {
        "name": "Riyafa",
        "hello": "world"
    };
    var err = prod->send(msg);
}

public function testSendMapInt() {
    map<int> msg = {
        "num": 1,
        "num2": 2
    };
    var err = prod->send(msg);
}

public function testSendMapFloat() {
    map<float> msg = {
        "numf1": 1.1,
        "numf2": 1.2
    };
    var err = prod->send(msg);
}

public function main() {
    map<byte> msg = {
        "byte1": 1,
        "byte2": 7
    };
    var err = prod->send(msg);
}

public function testSendMapBoolean() {
    map<boolean> msg = {
        "first": true,
        "second": false
    };
    var err = prod->send(msg);
}

public function testSendMapByteArray() {
    byte[3] array1 = [1, 2, 3];
    byte[1] array2 = [5];
    map<byte[]> msg = {
        "array1": array1,
        "array2": array2
    };
    var err = prod->send(msg);
}

public function testSendProperties() {
    artemis:Message msg = new(session, "Properties' test");
    msg.putProperty("string", "Hello again!");
    msg.putProperty("int", 123);
    msg.putProperty("float", 1.111);
    msg.putProperty("boolean", true);
    byte byteVal = 1;
    msg.putProperty("byte", byteVal);
    byte[4] arr = [1,1,0,0];
    msg.putProperty("byteArray", arr);
    
    var err = prod->send(msg);
}
