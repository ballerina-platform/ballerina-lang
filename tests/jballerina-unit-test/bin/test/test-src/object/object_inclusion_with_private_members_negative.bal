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

class ClassWithPrivateField {
    private int i = 0;

    function fn() {
    }
}

class Class1 {
    *ClassWithPrivateField;

    function init(int x) {
        self.i = x;
    }

    function fn() {
    }
}

object {} objectConstr1 = object ClassWithPrivateField {
    function init() {
        self.i = 2;
    }

    function fn() {
    }
};

type ObjectTypeDesc1 object {
    *ClassWithPrivateField;
};

class ClassWithPrivateMethod {
    int i = 0;

    private function fn() {
    }

    function fn2() {
    }
}

class Class2 {
    *ClassWithPrivateMethod;
    int m = 1;

    function init(int x) {
        self.i = x;
    }

    private function fn() {
    }

    function fn2() {
    }
}

object {} objectConstr2 = object ClassWithPrivateMethod {
    function init() {
        self.i = 2;
    }

    private function fn() {
    }

    function fn2() {
    }
};

type ObjectTypeDesc2 object {
    *ClassWithPrivateMethod;
    int x;
};

public class ClassWithPrivateFieldsAndMethods {
    int h = 0;
    private int j = 1;
    public int k = 2;

    private function fn() {
    }

    function fn2() {
    }

    public function fn3() {
    }
}

class Class3 {
    *ClassWithPrivateFieldsAndMethods;

    function init(int x) {
        self.h = x;
        self.j = 2 * x;
        self.k = 3 * x;
    }

    private function fn() {
    }

    function fn2() {
    }

    public function fn3() {
    }
}

object {} objectConstr3 = object ClassWithPrivateFieldsAndMethods {
    function init() {
        self.h = 2;
        self.j = 3;
        self.k = 4;
    }

    private function fn() {
    }

    function fn2() {
    }

    public function fn3() {
    }
};

type ObjectTypeDesc3 object {
    *ClassWithPrivateFieldsAndMethods;
    *ClassWithPrivateField;
};

class ClassWithMultipleFieldsIncludingPrivateFields {
    int i = 0;
    private int j = 0;
    private int k = 0;

    function fn() {
    }
}

client class ClassWithMultipleMethodsIncludingPrivateMethods {
    int i = 0;

    remote function fn() {
    }

    private function fn2() {
    }
}

function fn() {
    var ob1 = object ClassWithMultipleFieldsIncludingPrivateFields {
        function init() {
            self.i = 1;
            self.j = 1;
            self.k = 1;
        }

        function fn() {
        }
    };

    var ob2 = client object ClassWithMultipleMethodsIncludingPrivateMethods {
        int i = 0;

        remote function fn() {
        }

        private function fn2() {
        }
    };
}
