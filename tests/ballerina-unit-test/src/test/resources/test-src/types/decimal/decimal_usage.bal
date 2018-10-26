// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


// Decimal array.
function testDecimalArray() returns (decimal[], int, decimal, decimal) {
    decimal[] dArr = [12.3, 23.2, 34.534, 5.4];
    int length = lengthof dArr;
    decimal element0 = dArr[0];
    decimal element1 = dArr[1];
    return (dArr, length, element0, element1);
}

// Decimal map.
function testDecimalMap() returns (map<decimal>, int, string[], decimal) {
    map<decimal> dMap = {element0: 12.45, element1: 34.3, element2: 2314.31};
    int length = lengthof dMap;
    string[] keys = dMap.keys();
    decimal element0 = dMap.element0;
    return (dMap, length, keys, element0);
}
