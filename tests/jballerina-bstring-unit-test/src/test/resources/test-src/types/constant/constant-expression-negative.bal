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
