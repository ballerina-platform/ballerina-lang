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

# Contains an if condition to test the code coverage

public function main() {
    // counter
    // test
    int a = 10;
    if (a == 10) {
        a = 1;
    } else {
        a = 2;
    }
   
    incrementValue(a);
}


// Prints hello
public function incrementValue(int a) {
    // Prints hello
    int b = a + 1;
}

// Record type definition
type Record record {
    int i;
    string description = "abc";
};

type INT int;

class ABC {
    private final readonly & Record rec;
    private final readonly & INT num;

    function init(int x) {
        self.rec = createRec(x);
        self.num = getInt(x);
    }

    function getRec() returns Record {
        return self.rec;
    }

    function getNum() returns int {
        return self.num;
    }
}

function createRec(int x) returns readonly & Record {
    readonly & Record rec = {i: x, description: "desc"};
    return rec;
}

function getInt(int x) returns readonly & int {
    return x;
}
