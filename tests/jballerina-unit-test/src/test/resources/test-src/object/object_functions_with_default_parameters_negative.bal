// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class Class1 {
    int i = 0;

    public function foo(int arg1 = i) { // error
        int _ = self.i;
    }
}

public class Class2 {
    int i = 0;

    public function foo(int i = i) { // error
        
    }
}

class Class3 {
    int i = 0;
    string s = "";

    public function foo(int arg1 = i, string arg2 = s) { // errors
        
    }
}

class Class4 {
    int i = 0;
    string s = "";
    string|int u;

    function init() {
        self.u = 2;
    }

    public function foo(int arg1 = i, string arg2 = s, string|int arg3 = u) { // errors
        
    }
}

public class Class5 {
    int i = 0;

    public function foo(int arg1 = i) { // error
        int _ = self.i;
    }

    public function bar() {
        self.foo(2);
    }
}

public class Class6 {
    int i = 0;

    public function foo(int arg1 = i) { // error
        object {} _ = object {
            int i = 0;

            function foo(int arg2 = i) { // error
                int _ = self.i;
            }
        };
    }
}

public class Class7 {
    int i = 0;

    public function foo(int arg1 = i) { // error
        object {} _ = object {

            function foo(int arg2 = i) { // error
            }
        };
    }
}

public class Class8 {
    int i = 0;

    public function foo(int arg1 = i) { // error
        object {} _ = object {
            int i = 0;

            function foo(int arg2 = i) { // error
                int _ = self.i;
                object {} _ = object {
                    int i = 0;

                    function foo(int arg3 = i) { // error
                        int _ = self.i;
                    }
                };
            }
        };
    }
}

type Object1 object {
    int i;
    function foo(int arg1 = i); // error
};

type Object2 object {
    int i;
    function foo(int arg1 = i); // error
    function bar(int arg1 = i); // error
};

class Class9 {
    int i = 0;
    string s = "";
    string|int u;

    function init() {
        self.u = 2;
    }

    public function foo(int arg1 = self.i, string arg2 = self.s, string|int arg3 = self.u) { // errors

    }
}
