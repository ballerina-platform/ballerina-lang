// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

public function queryExpressionTest1() {
    int[] nums = [1, 2, 3, 4];

    int[] evenNums = from var i in nums
                     where i % 2 == 0
                     select ;

    int[] evenNums = from int i in  where i % 2 == 0 select ;

}

function testStringType() {
    string student = "John";
    from  in student
}

function testXMLType() {
    xml data = xml `XML Data`;
    from  in data
}

function testStringTypeExpression() {
    string students = "Jane, Will";
    from string name in ;
}
