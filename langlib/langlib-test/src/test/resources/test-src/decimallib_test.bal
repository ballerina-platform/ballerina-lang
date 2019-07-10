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

import ballerina/'lang\.decimal as decimals;


function testSum(decimal p1, decimal p2) returns decimal {
    return decimals:sum(p1, p2);
}

function testOneArgMax(decimal arg) returns decimal {
    return decimals:max(arg);
}

function testMultiArgMax(decimal arg, decimal[] otherArgs) returns decimal {
    return decimals:max(arg, ...otherArgs);
}