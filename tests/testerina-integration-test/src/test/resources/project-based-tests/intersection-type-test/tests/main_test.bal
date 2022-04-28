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

import ballerina/test;

type ImmutableRecordType2 readonly & RecordType;

type ImmutableObjectType readonly & ObjectType;

type ImmutableObjectOrRecordType readonly & ObjectOrRecordType;

type ImmutableTupleType readonly & TupleType;

type ImmutableArrayType readonly & ArrayType;

readonly class ObjectClass {
    final RecordType & readonly rec;
    final readonly & Uuid uuid;

    function init(RecordType & readonly val, Uuid & readonly id) {
        self.rec = val;
        self.uuid = id;
    }
}

type EmployeeRec record {
    Uuid id;
    string name;
};

@test:Config {
}
function testIntersectionTypes() {
    Uuid uuid = {timeLow: 1, timeMid: 2, timeHiAndVersion: 3, clockSeqHiAndReserved: 4, clockSeqLo: 5, node: 6};
    Uuid returnedUuid = getUuid();
    test:assertEquals(returnedUuid, uuid);

    ImmutableRecordType2 recordVal = {id: 1};
    ImmutableRecordType returnedRecord = getRecordValue();
    test:assertEquals(returnedRecord, recordVal);

    ImmutableObjectType objVal = new ObjectClass(recordVal, uuid);
    test:assertEquals(objVal.rec.id, 1);

    ImmutableTupleType tupleVal = [10, "abc"];
    test:assertEquals(tupleVal[0], 10);

    ImmutableArrayType arrayVal = [recordVal];
    test:assertEquals(arrayVal[0].id, 1);

    ImmutableObjectOrRecordType _ = objVal;

    EmployeeRec & readonly employeeVal = {id: uuid, name: "Chiran"};
    test:assertEquals(employeeVal.name, "Chiran");
}
