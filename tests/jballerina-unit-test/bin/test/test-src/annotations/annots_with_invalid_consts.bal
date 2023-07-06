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
    Foo[] f;
    int i = 10;
|};

public const annotation Bar v1 on source service, source listener;

string s2 = "s2";

service object {} ser2 = @v1 {
    f: [
        { s: "s", t: "t" },
        { s: s2, t: "t2" }
    ]
} service object {
    resource function get res() {

    }
};

type Baz record {
    string st;
    Qux q?;
};

class Qux {

}

public const annotation Baz v2 on service, source listener;

service object {} ser3 = @v2 {
    st: "string value"
} service object  {
    resource function get res() {

    }
};

// public const annotation record {| int increment = 1; |} v3 on source type;

// int x = 1;

// @v3 {
//     increment: -x
// }
// type Quux record {|
//     int x;
// |};

const annotation record {| int[] arr; |}[] v3 on type;

int[] intArr = [1, 2];
int i = 1;
const TWO = 2;

@v3 {
    arr: intArr
}
@v3 {
    arr: [TWO, i]
}
@v3 {
    arr: [i, i]
}
type Corge record {|
    int a;
    int b;
|};
