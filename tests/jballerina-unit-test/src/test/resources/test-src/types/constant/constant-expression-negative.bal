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

const CI1 = 1;

const CI2 = 1 + 2;      // type is required for constants with expressions

const map<int> CIMap1 = { v1 : 1 + 2, v2 : 2 * 5}; // const expressions are not yet supported here

const int CI3  = 1/0; // invalid constant expression, reason '/ by zero'

const int CI4  = 0/0; // invalid constant expression, reason '/ by zero'

const int CI5  = *(5 + 10); // expression is not a constant expression

const string CS1 = +("hello" + "world"); // operator '+' not defined for 'string'

const boolean CB1 = !(10); // operator '!' not defined for 'int'

const int CONST1 =  ~~false; // operator '~' not defined for 'boolean'

const int CONST2 =  -!false; // operator '-' not defined for 'boolean'

const int CONST3 =  -~false; // operator '~' not defined for 'boolean'

const int A = D + B;
const int C = A;
const int B = C;
const int D = 1;

const float E = F;
const float F = E;

