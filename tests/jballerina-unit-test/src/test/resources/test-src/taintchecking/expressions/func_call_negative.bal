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

public function main(int i, string s, boolean b1, boolean b2) {
    bar(i, s);

    bar(i, s, b1, b2);

    boolean[] bArr = [b1, b2];
    bar(i, s, ...bArr);

    bar(i, s, b1, ...bArr);

    var w = [s, b1, b2];
    bar(i, ...w);

    [int, string, boolean] x = [i, s, b1];
    bar(...x);

    [int, string, boolean, boolean] y = [i, s, b1, b2];
    bar(...y);

    [int, string] z = [i, s];
    bar(...z);
    baz(...z);
}

function bar(@untainted int i, @untainted string s, @untainted boolean... b) {
}

function baz(@untainted int i, @untainted string s) {
}
