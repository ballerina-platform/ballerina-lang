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

import ballerina/lang.'object;

class Iterable {
    *object:Iterable;
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
    Iterable p = new Iterable();
    int[] integers = from var item in p
                     select item;

    return integers;
}

class AnotherIterable {
    *object:Iterable;
    public function iterator() returns object {

        public isolated function next() returns record {|Iterable value;|}?;
    } {
        return object {
            int cursorIndex = 0;
            public isolated function next() returns record {|Iterable value;|}? {
                self.cursorIndex += 1;
                if (self.cursorIndex <= 2) {
                    return {
                        value: new Iterable()
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

class IterableWithError {
    *object:Iterable;
    public function iterator() returns object {

        public isolated function next() returns record {|int value;|}|error?;
    } {
        return object {
            int[] integers = [12, 34, 56, 34, 78];
            int cursorIndex = 0;
            public isolated function next() returns record {|int value;|}|error? {
                self.cursorIndex += 1;
                if (self.cursorIndex == 3) {
                    return error("Custom error thrown.");
                }
                else if (self.cursorIndex <= 5) {
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

public function testIterableWithError() {
    IterableWithError p = new IterableWithError();
    int[]|error integers = from var item in p
                     select item;

    if (integers is error) {
        return;
    }
    panic error("Expected error, found: " + (typeof integers).toString());
}

class NumberGenerator {
    int i = 0;
    public isolated function next() returns record {| int value; |}? {
        self.i += 1;
        if(self.i < 5) {
          return { value: self.i };
        }
    }
}

class NumberStreamGenerator {
    int i = 0;
    public isolated function next() returns record {| stream<int> value; |}? {
         self.i += 1;
         if (self.i < 5) {
             NumberGenerator numGen = new();
             stream<int> numberStream = new (numGen);
             return { value: numberStream};
         }
    }
}

public function testStreamOfStreams() returns int[] {
    NumberStreamGenerator numStreamGen = new();
    stream<stream<int>> numberStream = new (numStreamGen);
    record {| stream<int> value; |}? nextStream = numberStream.next();
    int[] integers = from var strm in numberStream
                     from var num in liftError(toArray(strm))
                     select <int>num;

    return integers;
}

function toArray (stream<any|error, error?> strm) returns any[]|error {
    any[] arr = [];
    record {| any|error value; |}|error? v = strm.next();
    while (v is record {| any|error value; |}) {
        any|error value = v.value;
        if (!(value is error)) {
            arr.push(value);
        }
        v = strm.next();
    }
    if (v is error) {
        return v;
    }
    return arr;
}

function liftError (any[]|error arrayData) returns any[] {
    if(!(arrayData is error)) {
        return arrayData;
    }
    return [];
}

public function testIteratorInStream() returns int[]|error {
    int[] intArray = [1, 2, 3, 4, 5];
    stream<int> numberStream = intArray.toStream();
    int[]|error integers = from var num in getIterableObject(numberStream.iterator())
                     select <int> checkpanic num;

    return integers;
}

public type _Iterator object {
    public isolated function next() returns record {|any|error value;|}|error?;
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

function getIterableObject(_Iterator iterator) returns IterableFromIterator {
    return new IterableFromIterator(iterator);
}

class MyIterable {
    *object:Iterable;
    public function iterator() returns object {
                                           public function next() returns record {| int value; |}?;
                                       } {
        return new MyIterator();
    }
}

int i = 0;

public class MyIterator {
    public function next() returns record {| int value; |}? {
        i += 1;
        if (i < 5) {
            return {value: i};
        }
        return ();
    }
}

public function testObjectIterator() {
    int[] expectedArr = [1, 2, 3, 4];
    int[] integers = from var item in new MyIterable()
                     select item;

    assertEquality(integers, expectedArr);
}

//---------------------------------------------------------------------------------------------------------
const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                      message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
