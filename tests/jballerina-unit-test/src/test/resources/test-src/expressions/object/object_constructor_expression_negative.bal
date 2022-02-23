// Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type SampleRec record {};

var objCreatedViaConstructor = object SampleRec {};

var notClient = object {
    private remote function remoteFunc() {}
};

var invalidInit = object {
    private function init(int x) {}
};

var objVariable = object {
    private function init() {
    }
};

var invalidKeyWord = public object {};

class MO {}

var testObjectWithTypeReference = object {
    *MO;
};

any o = object {  };

distinct class DistinctFoo {
    int i = 0;
}

distinct class DistinctFooA {
    int i = 0;
}

function testObjectConstructorWithoutDefiniteTypeAndReferenceVar() {
    DistinctFooA|DistinctFoo distinctObject = object {
                                        int i;
                                        function init() {
                                            self.i = 20;
                                        }
                                    };
}

//////  Test object-constructor-expr with `readonly` contextually expected type //////

readonly class ReadOnlyClass {
    int a;
    string[] b;

    function init(int a, string[] & readonly b) {
        self.a = a;
        self.b = b;
    }
}

type Object object {
    int a;
    string[] b;
};

string[] mutableStringArr = [];
string[] & readonly immutableStringArr = [];

function testInvalidObjectConstructorExprWithReadOnlyCET() {
    ReadOnlyClass v1 = object {
        int a = 1;
        string[] b = mutableStringArr; // incompatible types: expected 'string[] & readonly', found 'string[]'
    };

    ReadOnlyClass v2 = object { // incompatible types: expected 'ReadOnlyClass', found 'object { final int a; final (string[] & readonly) s; } & readonly'
        int a = 1;
        string[] s = mutableStringArr; // incompatible types: expected 'string[] & readonly', found 'string[]'
    };

    Object & readonly v3 = object {
        int a = 1;
        string[] b = mutableStringArr; // incompatible types: expected 'string[] & readonly', found 'string[]'
        stream<string>? c = immutableStringArr.toStream(); // incompatible types: expected '()', found 'stream<string>'
    };

    Object & readonly v4 = object {
        int a = 1;
        string[] b;
        stream<string>? c;

        function init() {
            self.b = mutableStringArr; // incompatible types: expected 'string[] & readonly', found 'string[]'
            self.c = immutableStringArr.toStream(); // incompatible types: expected '()', found 'stream<string>'
        }
    };

    readonly & object {
        string[] a;
        stream<string>? b;
    } v5 = object {
        string[] a = mutableStringArr; // incompatible types: expected 'string[] & readonly', found 'string[]'
        stream<string>? b;

        function init() {
            self.b = immutableStringArr.toStream(); // incompatible types: expected '()', found 'stream<string>'
        }
    };
}

type SerT service object {
    function onMessage();
};

function foo() {
    SerT s = service object SerT {

    };
}

type Bar readonly & object {
    any i;
};

stream<int> s = new;

function testObjectConstructorWithReferredIntersectionType() {
    Bar b = object {
        any i = s;
        int j;

        function init() {
            self.j = 1;
        }
    };
}
