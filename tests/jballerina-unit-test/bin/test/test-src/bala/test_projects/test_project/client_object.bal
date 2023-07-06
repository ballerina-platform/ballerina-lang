// Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
public client class DummyEndpoint {
    remote function invoke1 (string a) returns error? {
        error e = error("i1");
        return e;
    }

    remote function invoke2 (string a) returns string {
        return "done" + a;
    }
}

DummyEndpoint dyEP = new;

public function getDummyEndpoint() returns DummyEndpoint {
    return dyEP;
}
