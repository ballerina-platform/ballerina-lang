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


class IteratorWithIsolatedNext {
    int i = 0;
    public isolated function next() returns record {|int value;|}|error? {
        if (self.i == 5) {
            return ();
        }
        self.i += 1;
        return {value: self.i};
    }
}

class IteratorWithIsolatedNextAndIsolatedClose {
    int i = 0;
    public isolated function next() returns record {|int value;|}|error? {
        if (self.i == 5) {
            return ();
        }
        self.i += 1;
        return {value: self.i};
    }

    public isolated function close() returns error? {

    }
}

class IteratorWithNonIsolatedNext {
    int i = 0;
    public function next() returns record {|int value;|}|error? {
        if (self.i == 5) {
            return ();
        }
        self.i += 1;
        return {value: self.i};
    }
}

class IteratorWithNonIsolatedNextAndIsolatedClose {
    int i = 0;
    public function next() returns record {|int value;|}|error? {
        if (self.i == 5) {
            return ();
        }
        self.i += 1;
        return {value: self.i};
    }

    public isolated function close() returns error? {

    }
}

class IteratorWithIsolatedNextAndNonIsolatedClose {
    int i = 0;
    public isolated function next() returns record {|int value;|}|error? {
        if (self.i == 5) {
            return ();
        }
        self.i += 1;
        return {value: self.i};
    }

    public function close() returns error? {

    }
}

public function main() returns @tainted error? {
    IteratorWithIsolatedNext itr1 = new;
    IteratorWithIsolatedNextAndIsolatedClose itr2 = new;
    IteratorWithNonIsolatedNext itr3 = new;
    IteratorWithNonIsolatedNextAndIsolatedClose itr4 = new;
    IteratorWithIsolatedNextAndNonIsolatedClose itr5 = new;
    var intStr1 = new stream<int, error>(itr1);
    var intStr2 = new stream<int, error>(itr2);
    var intStr3 = new stream<int, error>(itr3);
    var intStr4 = new stream<int, error>(itr4);
    var intStr5 = new stream<int, error>(itr5);
}
