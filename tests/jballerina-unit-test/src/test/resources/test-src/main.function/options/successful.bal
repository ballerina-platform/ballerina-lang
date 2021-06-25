// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License;
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing;
// software distributed under the License is distributed on an
// "AS IS" BASIS; WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND; either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

public type Options record {|
    int a;
    float b;
    decimal c;
    string d;
    boolean m;
    int? e;
    float? f;
    decimal? g;
    string? h;
    boolean? n;
    int[] p;
    float[] q;
    decimal[] r;
    string[] s;
    boolean[] t;
    int i = 1;
    float j = 2.0;
    decimal k = 1e100;
    string l = "l";
    boolean o = false;
    int? u = 3;
    float? v = 3.0;
    decimal? w = 4e100;
    string? x = "str";
    boolean? y = false;
    int[] z = [4,5];
    float[] qa = [6.0];
    decimal[] rb = [7e100, 8e100];
    string[] sc =[];
    boolean[] te = [true, false];
|};
public function main(*Options options) {
}
