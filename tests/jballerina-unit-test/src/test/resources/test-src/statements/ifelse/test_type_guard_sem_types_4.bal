// Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

public type Position int;

public type PositionFields record {|
   Position startPos;
   Position endPos;
|};

public type VarRefExpr record {|
    *PositionFields;
    string? prefix;
    string name;
    Position qNamePos;
|};

public const WILDCARD = ();

public type MemberAccessLExpr record {|
    *PositionFields;
    Position opPos;
    LExpr container;
    string|int index; // changed
|};

public type FieldAccessLExpr record {|
    *PositionFields;
    Position opPos;
    LExpr container;
    string fieldName;
|};

public type OtherExpr record {|
    *PositionFields;
    string? prefix;
    string name;
    string qNamePos;
|};


public type LExpr VarRefExpr|MemberAccessLExpr|FieldAccessLExpr;

function f1(LExpr|WILDCARD lValue) {
    if lValue is VarRefExpr {

    } else if lValue is WILDCARD {

    } else {
        MemberAccessLExpr|FieldAccessLExpr _ = lValue; // OK
    }
}

function f2(LExpr lValue) {
    if lValue is VarRefExpr {

    } else {
        MemberAccessLExpr|FieldAccessLExpr _ = lValue; // OK
    }
}

function f3(VarRefExpr|WILDCARD|OtherExpr lValue) {
    if lValue is VarRefExpr {

    } else if lValue is WILDCARD {

    } else {
        OtherExpr _ = lValue; // error incompatible types: expected 'OtherExpr', found '(VarRefExpr|OtherExpr)'
    }
}
