// Copyright (c) 2026 WSO2 LLC. (http://www.wso2.com).
//
// WSO2 LLC. licenses this file to you under the Apache License,
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

import testorg/bir_symbol_version_lib as lib;

public type Person record {
    string name;
    int age;
    lib:StatusResult status;
};

final readonly & Person person = {
    name: "John Doe",
    age: 30,
    status: {status: lib:ACTIVE}
};

final readonly & lib:StatusResultWithVersion statusResultWithVersion = {
    status: {status: lib:ACTIVE},
    version: "1.0.0"
};

@lib:ResourceDefinition{
    resourceType: "person",
    statusResultWithVersion: {
        status: {status: lib:ACTIVE},
        version: "1.0.0"
    }
}
public type Person2 record {
    string name;
    int age;
    lib:StatusResult status;
};


public function getStatus() returns lib:Status {
    return lib:ACTIVE;
}
