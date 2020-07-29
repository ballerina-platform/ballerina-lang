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

import ballerina/lang.test as test;

type Iterable object {
    public function __iterator() returns abstract object {public function next() returns record {|int value;|}?;
    } {
        object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public function next() returns
            record {|
                int value;
            |}? {
                self.cursorIndex += 1;
                if (self.cursorIndex <= 7) {
                    return {
                        value: self.integers[self.cursorIndex - 1]
                    };
                } else {
                    return ();
                }
            }
        } sample = new;
        return sample;
    }
};

public function testIterableObject() {
    Iterable p = new Iterable();
    int[] integers = [];
    foreach var item in p {
        integers.push(item);
    }
    test:assertEquals(integers, [12, 34, 56, 34, 78, 21, 90]);
}

type AnotherIterable object {
    public function __iterator() returns abstract object {public function next() returns record {|Iterable value;|}?;
    } {
        object {
            int cursorIndex = 0;
            public function next() returns
            record {|
                Iterable value;
            |}? {
                self.cursorIndex += 1;
                if (self.cursorIndex <= 2) {
                    return {
                        value: new Iterable()
                    };
                } else {
                    return ();
                }
            }
        } sample = new;
        return sample;
    }
};

public function testNestedIterableObject() {
    AnotherIterable p = new AnotherIterable();
    int[] integers = [];
    foreach var itr in p {
        foreach var item in itr {
            integers.push(item);
        }
    }
    test:assertEquals(integers, [12, 34, 56, 34, 78, 21, 90,12, 34, 56, 34, 78, 21, 90]);
}
