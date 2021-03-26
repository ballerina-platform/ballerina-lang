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

import ballerina/lang.'object;

class IterableObject {
    public function iterator() returns object {

        public isolated function next() returns record {|int value;|}?;
    } {
        return object {
            int[] integers = [12, 34, 56, 34, 78, 21, 90];
            int cursorIndex = 0;
            public isolated function next() returns record {|int value;|}? {
                self.cursorIndex += 1;
                if (self.cursorIndex <= 7) {
                    return {
                        value: self.integers[self.cursorIndex - 1]
                    };
                } else {
                    return ();
                }
            }
        };
    }
}

public function testIterableObject() returns int[] {
    IterableObject p = new IterableObject();
    int[] integers = from var item in p select item;

    return integers;
}

class AnotherIterable {
    *object:Iterable;
    public function iterator() returns object {

        public isolated function next() returns record {|IterableObject value;|}?;
    } {
        return object {
            int cursorIndex = 0;
            public isolated function next() returns record {|IterableObject value;|}? {
                self.cursorIndex += 1;
                if (self.cursorIndex <= 2) {
                    return {
                        value: new IterableObject()
                    };
                } else {
                    return ();
                }
            }
        };
    }
}

public function testNestedIterableObject() returns int[] {
    AnotherIterable p = new AnotherIterable();
    int[] integers = from var itr in p
                     from var item in itr
                     select item;

    return integers;
}

public type _Iterator object {
    public isolated function next1() returns record {|any|error value;|}|error?;
};

class IterableFromIterator {
        *object:Iterable;
        _Iterator itr;
        public function init(_Iterator itr) {
            self.itr = itr;
        }

        public function iterator() returns _Iterator {
            return self.itr;
        }
}
