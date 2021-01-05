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

int i = 100;
isolated int j = 200;

public function nonIsolatedFunction() returns int => i;

public isolated function isolatedFunction() returns int {
    lock {
        return j;
    }
}

public type NonIsolatedObjectType object {
    public int i;
    public boolean j;
};

public type IsolatedObjectType isolated object {
    public int i;
    public boolean j;
};

public class NonIsolatedClass {
    int i = 1;

    public function getI() returns int => self.i;
}

public class NonIsolatedClassWithNonIsolatedInit {
    int i = 1;

    public function init() {

    }
}

public isolated class IsolatedClassWithExplicitIsolatedInit {
    public final int i = 1;
    final IsolatedObjectType j;

    public isolated function init(IsolatedObjectType j) {
        self.j = j;
    }

    public isolated function getJ() returns IsolatedObjectType => self.j;
}

public isolated class IsolatedClassWithExplicitNonIsolatedInit {
    public final int i = 1;
    final IsolatedObjectType j;

    public function init(IsolatedObjectType j) {
        self.j = j;
    }
}

public isolated class IsolatedClassWithoutInit {
    public final int i = 10;
    final IsolatedObjectType j = isolated object {
        public final int i = 20;
        public final boolean j = true;
    };
    private string k = "hello";

    public isolated function getJ() returns IsolatedObjectType => self.j;

    public isolated function getK() returns string {
        lock {
            return self.k;
        }
    }
}
