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

@typeParam
type Type any|error;

@typeParam
type Type1 any|error;

@typeParam
type PureType anydata|error;

type ArrayIterator object {

    private Type[] m;

    public function __init(Type[] m) {
        self.m = m;
    }

    public function next() returns record {|
        Type value;
    |}? = external;
};

public function length((any|error)[] arr) returns int = external;

# Returns an iterator over the elements of `arr`.
public function iterator(Type[] arr) returns abstract object {
    public function next() returns record {|
        Type value;
    |}?;
    } {
    ArrayIterator arrIterator = new(arr);
    return arrIterator;
}

public function enumerate(Type[] arr) returns [int, Type][] = external;

// Functional iteration

public function 'map(Type[] arr, function(Type val) returns Type1 func) returns Type1[] = external;

public function forEach(Type[] arr, function(Type val) returns () func) returns () = external;

public function filter(Type[] arr, function(Type val) returns boolean func) returns Type[] = external;

public function reduce(Type[] arr, function(Type1 accum, Type val) returns Type1 func, Type1 initial) returns Type1 = external;

// Sub-array


// TODO Fix this.
public function slice(Type[] arr, int startIndex, int endIndex) returns Type[] = external;

public function remove(Type[] arr, int i) returns Type = external;

public function removeAll((any|error)[] arr) returns () = external;

public function setLength((any|error)[] arr, int i) returns () = external;

public function indexOf(PureType[] arr, PureType val, int startIndex = 0) returns int? = external;

public function reverse(Type[] arr) returns Type[] = external;

public function sort(Type[] arr, function(Type val1, Type val2) returns int func) returns Type[] = external;

public function pop(Type[] arr) returns Type = external;

public function push(Type[] arr, Type... vals) returns () = external;

public function shift(Type[] arr) returns Type = external;

public function unshift(Type[] arr, Type... vals) returns () = external;

// Conversion

public function toBase64(byte[] arr) returns string = external;

public function fromBase64(string str) returns byte[]|error = external;

public function toBase16(byte[] arr) returns string = external;

public function fromBase16(string str) returns byte[]|error = external;