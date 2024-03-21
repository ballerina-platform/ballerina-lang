// Copyright (c) 2024 WSO2 LLC. (http://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

public function testMultipleReceiveDuplicateKey1() {
    worker w1 {
        50 -> w3;
    }

    worker w2 {
        100 -> w3;
    }

    worker w3 {
        _ = <- {a: w1, a: w2}; // error: duplicate key
    }
}

public function testMultipleReceiveDuplicateKey2() {
    worker w1 {
        50 -> w2;
        100 -> w2;
    }

    worker w2 {
        _ = <- {a: w1, a: w1}; // error: duplicate key
    }
}

type MapOfInt map<int>;

type IntType int;

type MapOfInt2 map<IntType>;

type ABRecord record {|int a; int b;|};

type ABCRecord record {|int a; int b; string c;|};

public function testMultipleReceiveTypeChecking1() {
    worker w2 {
        // Non-union cases
        int _ = <- {a: w1, b: w1}; // error: incompatible type
        map<int> _ = <- {a: w1, b: w1}; // OK
        record {|int c; int a; int b;|} _ = <- {a: w1, b: w1}; // error: incompatible type
        record {|int c?; int a; int b;|} _ = <- {a: w1, b: w1}; // OK
        record {|int a; int b;|} _ = <- {a: w1, b: w1}; // OK
        record {|int a; int b; int...;|} _ = <- {a: w1, b: w1}; // OK
        record {|int...;|} _ = <- {a: w1, b: w1}; // OK
        record {||} _ = <- {a: w1, b: w1}; // error: incompatible type
        record {} _ = <- {a: w1, b: w1}; // OK
        MapOfInt _ = <- {a: w1, b: w1}; // OK
        MapOfInt2 _ = <- {a: w1, b: w1}; // OK
        ABRecord _ = <- {a: w1, b: w1}; // OK
        ABCRecord _ = <- {a: w1, b: w1}; // error: incompatible type

        // Union cases
        string|int|map<int> _ = <- {a: w1, b: w1}; // OK
        string|record {int a; int b;}|int _ = <- {a: w1, b: w1}; // OK
        string|record {int a; int b; int c;}|int _ = <- {a: w1, b: w1}; // error: incompatible type
        string|boolean|int _ = <- {a: w1, b: w1}; // error: incompatible type
        map<int>|record {int a; int b;} _ = <- {a: w1, b: w1}; // error: ambiguous type
        map<int>|record {|int ...;|} _ = <- {a: w1, b: w1}; // error: ambiguous type
        map<int>|record {int a; int c;} _ = <- {a: w1, b: w1}; // OK
        int|record {int a; int c;} _ = <- {a: w1, b: w1}; // error: incompatible type

        // Special cases
        readonly r = <- {a: w1, b: w1}; // OK
        json _ = <- {a: w1, b: w1}; // OK
        anydata _ = <- {a: w1, b: w1}; // OK
        any _ = <- {a: w1, b: w1}; // OK
        record {|int a; int b;|} & readonly _ = <- {a: w1, b: w1}; // OK
        record {|int a; int c;|} & readonly _ = <- {a: w1, b: w1}; // error: incompatible type
    }

    worker w1 {
        1 ->> w2;
        2 ->> w2;
    }
}
