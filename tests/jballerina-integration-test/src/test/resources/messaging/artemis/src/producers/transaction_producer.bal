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
import ballerina/io;

public function testSimpleTransactionSend() {
    artemis:Producer prod = new({host: "localhost", port: 61616}, "example");
    send(prod);
    transaction {
        send(prod);
    }
}

public function testErringSend() {
    artemis:Producer prod = new({host: "localhost", port: 61616}, "example3");
    send(prod);
    transaction {
        send(prod);
        error err = error("Failed during send");
        panic err;
    }
}

public function testTransactionSend() {
    artemis:Connection con = new("tcp://localhost:61616");
    artemis:Session session = new(con);
    artemis:Producer prod = new(session, "example2");
    transaction {
        send(prod);
        send(prod);
    }
}

function send(artemis:Producer prod) {
    var err = prod->send("Example ");
    if (err is error) {
        io:println("Error occurred sending message");
    }
}
