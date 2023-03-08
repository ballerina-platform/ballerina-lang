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

public type Foo record {|
    string s;
    string t;
    boolean b = false;
|};

public type Bar record {|
    Foo[] f1;
    [Foo, string] f2;
    int i = 10;
|};

public const annotation Bar v1 on source service, source listener;

const s2 = "s2";
const t3 = "t3";

service object {} ser = @v1 {
    f1: [
        { s: "s", t: "t" },
        { s: s2, t: "t2" }
    ],
    f2: [{ s: "s3", t: t3 }, "test"]
} service object {
    resource function get res() {

    }
};

const annotation record {| int[] arr; |} v2 on type;

@v2 {
    arr: [1, 2]
}
type Baz record {|
    int a;
    int b;
|};
