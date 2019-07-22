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
import ballerina/io;

public type Employee record {
    string name;
};

public function main(int[] intArr, string[] strArr, float[] floatArr, boolean[] boolArr, json[] jsonArr,
                     Employee[] empArr) {
    io:print("integer: " + intArr[1].toString() + ", string: " + strArr[1] + ", float: " + floatArr[1].toString()  +
                ", boolean: " + boolArr[1].toString() + ", json: " + jsonArr[1].toString() + ", Employee Name Field: " +
                empArr[1].name);
}
