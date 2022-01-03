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

import annots/defn;

public const annotation Allow on parameter;

public function func(@Allow int i) {
}

public function otherFunc(int i, @defn:Annot {i: 456} int j = 1, @Allow int... k) {
}

public class TestListener {
    public function init(string token, @defn:Expose @Allow int? listenOn = 8090) {
    }

    public function attach(@defn:Annot {i: 1} service object {} s, string[]|string? name) returns error? {
    }

    public function detach(@defn:Annots {i: 1} @defn:Annots {i: 2} service object {} s) returns error? {
    }

    public function 'start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

    public isolated function immediateStop() returns error? {
    }
}
