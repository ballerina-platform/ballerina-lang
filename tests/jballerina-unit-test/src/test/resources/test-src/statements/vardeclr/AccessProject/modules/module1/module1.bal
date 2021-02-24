//  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
//  WSO2 Inc. licenses this file to you under the Apache License,
//  Version 2.0 (the "License"); you may not use this file except
//  in compliance with the License.
//  You may obtain a copy of the License at
//
//    http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

int number = 54;
public const int constInt = 55;
public function getNumber() returns int {
    return number;
}

public string name = "Ballerina";

public [byte, float] [byteValue, floatValue] = [32, 2.5];

type studentRecord record {
    int Id;
    string studentName;
};

public studentRecord {Id, studentName, ...studentDetail} = {Id: 1001, studentName: "John", "Age": 24, "surName": "Paker"};

type myError error<record{ int riskLevel; }>;

public myError error(errorMsg, error(errorCause), riskLevel = riskLevel) =
                                        error myError("AssignmentFailed", error("ArrayIndexOutOfBound"), riskLevel = 1);
