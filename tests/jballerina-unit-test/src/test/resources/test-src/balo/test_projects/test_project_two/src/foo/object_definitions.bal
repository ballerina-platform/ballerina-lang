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

public class ObjectWithModuleLevelVisibilityField {
    public int i;
    boolean b;

    public function init(int i, boolean b) {
        self.i = i;
        self.b = b;
    }

    public function getInt() returns int {
        return self.i;
    }
}

public class ObjectWithModuleLevelVisibilityMethod {
    public int i;
    public boolean b;

    public function init(int i, boolean b) {
        self.i = i;
        self.b = b;
    }

    function getInt() returns int {
        return self.i;
    }
}

public class ObjectWithPublicFieldsAndMethods {
    public int i;
    public boolean b;

    public function init(int i, boolean b = true) {
        self.i = i;
        self.b = b;
    }

    public function getInt() returns int {
        return self.i;
    }
}

public function getObjectWithModuleLevelVisibilityField() returns ObjectWithModuleLevelVisibilityField => new (1, true);

public function getObjectWithModuleLevelVisibilityMethod() returns ObjectWithModuleLevelVisibilityMethod =>
    new (2, false);

public function getObjectWithPublicFieldsAndMethods() returns ObjectWithPublicFieldsAndMethods => new (3);
