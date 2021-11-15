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

const constValue = 1000;

# String
public const string strConst = "Value";

# Int
@constDecl
public const int intConst = 10;

# Float
@constDecl
const float floatConst = 12.3;

# Byte
@constDecl
const byte byteConst = 2;

# Boolean
@constDecl
const boolean boolConst = true;

annotation constDecl;
