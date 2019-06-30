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

artemis:EndpointConfiguration config = {host: "localhost", port: 61616};
artemis:AddressConfiguration addressConfig = {autoCreated:false};

public function testSendString() {
    artemis:Producer prod = new(config , "queue1", addressConfig = addressConfig);
    var err = prod->send("Hello World");
}

public function testSendJson() {
    artemis:Producer prod = new(config, "queue2", addressConfig = addressConfig);
    json name = {"name": "Riyafa"};
    var err = prod->send(name);
}

public function testSendXml() {
    artemis:Producer prod = new(config, "queue3", addressConfig = addressConfig);
     xml book = xml `<book>The Lost World</book>`;
    var err = prod->send(book);
}

public function testSendRecord() {
    artemis:Producer prod = new(config, "queue4", addressConfig = addressConfig);
    json person = {"name": "John", "age":20};
    var err = prod->send(person);
}

public function testSendByteArray() {
    artemis:Producer prod = new(config, "queue6", addressConfig = addressConfig);
    byte[6] msg = [1, 2, 2, 3, 3, 2];
    var err = prod->send(msg);
}

public function testSendMapString() {
    artemis:Producer prod = new(config, "queue7", addressConfig = addressConfig);
    map<string> msg = {
        "name": "Riyafa",
        "hello": "world"
    };
    var err = prod->send(msg);
}

public function testSendMapInt() {
    artemis:Producer prod = new(config, "queue8", addressConfig = addressConfig);
    map<int> msg = {
        "num": 1,
        "num2": 2
    };
    var err = prod->send(msg);
}

public function testSendMapFloat() {
    artemis:Producer prod = new(config, "queue9", addressConfig = addressConfig);
    map<float> msg = {
        "numf1": 1.1,
        "numf2": 1.2
    };
    var err = prod->send(msg);
}

public function testSendMapByte() {
    artemis:Producer prod = new(config, "queue10", addressConfig = addressConfig);
   map<byte> msg = {
        "byte1": 1,
        "byte2": 7
    };
    var err = prod->send(msg);
}

public function testSendMapByteArray() {
    artemis:Producer prod = new(config, "queue11", addressConfig = addressConfig);
    byte[3] array1 = [1, 2, 3];
    byte[1] array2 = [5];
    map<byte[]> msg = {
        "array1": array1,
        "array2": array2
    };
    var err = prod->send(msg);
}

public function main() {
    artemis:Producer prod = new(config, "queue12", addressConfig = addressConfig);
    map<boolean> msg = {
        "first": true,
        "second": false
    };
    var err = prod->send(msg);
}
