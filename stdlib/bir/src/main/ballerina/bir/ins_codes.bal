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

public const int INS_GOTO = 1;
public const int INS_CALL = 2;
public const int INS_BRANCH = 3;
public const int INS_RETURN = 4;

// Non-terminating instructions
public const int INS_MOVE = 5;
public const int INS_CONST_LOAD = 6;
public const int INS_NEW_MAP = 7;
public const int INS_MAP_STORE = 8;
public const int INS_MAP_LOAD = 9;
public const int INS_NEW_ARRAY = 10;
public const int INS_ARRAY_STORE = 11;
public const int INS_ARRAY_LOAD = 12;
public const int INS_NEW_ERROR = 13;
public const int INS_TYPE_CAST = 14;
public const int INS_IS_LIKE = 15;
public const int INS_TYPE_TEST = 16;


// Binary expression related instructions.
public const int INS_ADD = 20;
public const int INS_SUB = 21;
public const int INS_MUL = 22;
public const int INS_DIV = 23;
public const int INS_MOD = 24;
public const int INS_EQUAL = 25;
public const int INS_NOT_EQUAL = 26;
public const int INS_GREATER_THAN = 27;
public const int INS_GREATER_EQUAL = 28;
public const int INS_LESS_THAN = 29;
public const int INS_LESS_EQUAL = 30;