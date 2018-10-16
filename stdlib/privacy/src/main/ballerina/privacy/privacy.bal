// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public function pseudonymize (PiiStore store, string pii) returns string|error {
    return store.pseudonymize(pii);
}

public function depseudonymize (PiiStore store, string id) returns string|error {
    return store.depseudonymize(id);
}

public function delete (PiiStore store, string id) returns ()|error {
    return store.delete(id);
}
