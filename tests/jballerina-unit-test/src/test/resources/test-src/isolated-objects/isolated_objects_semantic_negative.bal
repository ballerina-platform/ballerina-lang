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

type Foo isolated object {
    int a;
    string[] b;
};

isolated class Bar {
    public final int[] & readonly a = [1, 2];
    final Foo & readonly b;
    private int c;
    private boolean[] d;

    isolated function init(Foo & readonly b, int c, boolean[] d) {
        self.b = b;
        self.c = c;
        self.d = d;
    }
}

isolated class Baz {
    public final int[] & readonly a = [1, 2];
    final Foo & readonly b = object {
        final int a = 1;
        final string[] & readonly b = [];
    };
    public final int c = 1;
    final readonly & boolean[] d = [];
}

isolated class Qux {
    public final int[] & readonly a = [1, 2, 3, 4];
    final Foo & readonly b = object {
        final int a = 1;
        final string[] & readonly b = [];
    };
}

class Quux {
    public final int[] & readonly a = [1, 2, 3, 4];
    final Foo & readonly b = object {
        final int a = 1;
        final string[] & readonly b = [];
    };
}

type Quuz record {
    Foo f;
};

function testIsolatedFunctionSemanticsNegative() {
    isolated object {} x = object {
        final int i = 1;
    };
    Foo v1 = x;
    Baz v2 = x;

    Foo & readonly readOnlyFoo = object {
                                     final int a = 1;
                                     final string[] & readonly b = [];
                                 };
    Bar bar = new (readOnlyFoo, 1234, [true, false]);

    Bar v3 = new Baz();
    Baz v4 = bar;

    Qux v5 = new Quux();

    Quuz v6 = {f: new Quux()};
}
