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

type XY record {
    int x;
    int y;
};

// any field other than x and y
type NotXY record {
    never x?;
    never y?;
};

function testInferredType(XY xy) returns XY {
    var {x: _, y: _, ...extra} = xy;
    return extra;
}

type ClosedXY record {|
    int x;
    int y;
    string...;
|};

function testDefinedRestType() returns map<int> {
    int xx;
    int yy;
    map<int|string> extra;
    {x: xx, y: yy, ...extra} = <ClosedXY>{x:10, y:20, "foo":"bar"};
    return extra;
}

type OptionalXY record {
    int x?;
    int y?;
};

function testWithOptionalFields() returns map<int> {
    var {...extra} = <XY>{x:10, y:20, "foo":"bar"};
    return extra;
}
