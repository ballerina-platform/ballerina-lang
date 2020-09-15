// Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

public distinct class DFoo {
    public int i = 0;
}

public distinct class DBar {
    *DFoo;

    public function init(int i) {
        self.i = i;
    }
}

public distinct class DA {
    *DB;

    public function init(int i, int j) {
        self.i = i;
        self.j = j;
    }
}

public distinct class DB {
    public int i = 0;
    public int j = 0;
}

public distinct class DY {
    *DX;
    *DOB;

    public function init(int i) {
        self.i = i;
    }
}

public class DX {
    public int i;

    public function init(int i) {
        self.i = i;
    }
}

distinct class DOB {

}