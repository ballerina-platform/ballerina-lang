// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package ballerina.builtin.transactions.coordinator;

import ballerina.test;

public function testProtocolCompatibility1 () {
    Protocol[] protos = [{name:"X"}, {name:"volatile"}, {name:"durable"}];
    boolean isCompatible = protocolCompatible(TWO_PHASE_COMMIT, protos);
    test:assertBooleanEquals(isCompatible, false, "Protocols should have been compatible");
}

public function testProtocolCompatibility2 () {
    Protocol[] protos = [{name:"volatile"}, {name:"x"}, {name:"durable"}];
    boolean isCompatible = protocolCompatible(TWO_PHASE_COMMIT, protos);
    test:assertBooleanEquals(isCompatible, false, "Protocols should have been incompatible");
}

public function testProtocolCompatibility3 () {
    Protocol[] protos = [{name:"volatile"}, {name:"durable"}, {name:"x"}];
    boolean isCompatible = protocolCompatible(TWO_PHASE_COMMIT, protos);
    test:assertBooleanEquals(isCompatible, false, "Protocols should have been incompatible");
}

public function testProtocolCompatibility4 () {
    Protocol[] protos = [{name:"volatile"}];
    boolean isCompatible = protocolCompatible(TWO_PHASE_COMMIT, protos);
    test:assertBooleanEquals(isCompatible, true, "Protocols should have been compatible");
}

public function testProtocolCompatibility5 () {
    Protocol[] protos = [{name:"volatile"}, {name:"completion"}, {name:"durable"}, {name:"foo"}];
    boolean isCompatible = protocolCompatible(TWO_PHASE_COMMIT, protos);
    test:assertBooleanEquals(isCompatible, false, "Protocols should have been incompatible");
}

public function testProtocolCompatibility6 () {
    Protocol[] protos = [{name:"volatile"}, {name:"completion"}, {name:"durable"}];
    boolean isCompatible = protocolCompatible(TWO_PHASE_COMMIT, protos);
    test:assertBooleanEquals(isCompatible, true, "Protocols should have been compatible");
}
