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

function query(string q, typedesc<record {|int...;|}> rowType = <>) returns stream<rowType, error> = external;

function testInvalidArgForInferTypedesc() {
    stream<OpenRecord, error> stm = query("");
    stream<OpenRecord, error> stm2 = query("", OpenRecord);
}

type OpenRecord record {

};

function queryWithMultipleInferTypedescs(string q, typedesc<record {|int...;|}> rowType = <>,
                                         typedesc<error> errorType = <>) returns stream<rowType, errorType> = external;

class ClassWithMethodWithMultipleInferTypedescs {
    function queryWithMultipleInferTypedescs(typedesc<record {}> rowType = <>, typedesc<error> errorType = <>)
                returns stream<rowType, errorType> = external;
}

stream<record {| int x; |}, error> stm = queryWithMultipleInferTypedescs("");

ClassWithMethodWithMultipleInferTypedescs cl = new;
stream<record {| int x; |}, error> stm2 = cl.queryWithMultipleInferTypedescs();
stream<record {| int x; |}, error> stm3 = cl.queryWithMultipleInferTypedescs(rowType = OpenRecord);
