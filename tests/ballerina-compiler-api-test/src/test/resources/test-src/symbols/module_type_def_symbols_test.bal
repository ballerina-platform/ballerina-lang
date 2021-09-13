// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

// Simple
@v1 public type nilType ();

@v1 public type boolType boolean;

@v1 public type intType int;

@v1 public type floatType float;

@v1 public type decimalType decimal;

// Sequence
@v1 public type stringType decimal;

@v1 public type xmlType xml;

// Structural
@v1 public type arrType string[];

@v1 public type tupleType [int];

@v1 public type mapType map<int>;

@v1 public type recType record {|
    string name;
    int age;
|};

@v1 public type tableType table<recType> key<string>;

// Behavioural
@v1 public type errorType error;

@v1 public type funcType function (string a, string b) returns string;

@v1 public type objType object {
    int id;
};

@v1 public type futureType future<string>;

@v1 public type typeDescType typedesc<futureType>;

@v1 public type handleType handle;

@v1 public type streamtype stream<string, int>;

// Other
@v1 public type typeRefType objType;

@v1 public type anyType any;

@v1 public type neverType never;

@v1 public type readonlyType readonly;

@v1 public type distinctObjType distinct object {
    string name;
};

@v1 public type unionType string|int;

@v1 public type interType string & readonly;

@v1 public type optionalType int?;

@v1 public type anyDataType anydata;

@v1 public type jsonType json;

@v1 public type byteType byte;

@v1 public type otherType (byteType);

public annotation v1 on type;
