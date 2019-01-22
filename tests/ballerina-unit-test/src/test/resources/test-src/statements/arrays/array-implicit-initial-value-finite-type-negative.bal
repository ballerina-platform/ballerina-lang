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

// int based finite type.
type allInitZero 0|1|2;
type allInitNonZero 1|2|3;

allInitZero[] ar1 = [];
allInitNonZero[] ar2 = [];

// float based finite type.
type allFloatZero 0.0|1.0|3.14;
type allFloatNonZero 1.0|3.143;

allFloatZero[] fAr1 = [];
allFloatNonZero[] fAr2 = [];

// boolean based finit type.
type bool true|false;
type bTrue true;

bool[] bAr1 = [];
bTrue[] bTrueAr1 = [];

// string based finite type.
type allStrInclEmpty ""|"a"|"b";
type allStrNonEmpty "a"|"b"|"c";

allStrInclEmpty[] strFiniteArr = [];
allStrNonEmpty[] strFiniteNonEmptyArr = [];

type U int|float?;
U[] u = [];
