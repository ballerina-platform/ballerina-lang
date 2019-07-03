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

artemis:Connection con = new("tcp://localhost:61616");
artemis:Session session = new(con);
artemis:Producer prod = new(session, "close_address");

public function testClose() returns (artemis:Producer, artemis:Session, artemis:Connection) {
    var err = prod->close();
    err = session->close();
    err = con->close();
    return (prod, session, con);
}
