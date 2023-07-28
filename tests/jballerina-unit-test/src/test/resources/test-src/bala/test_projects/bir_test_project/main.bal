// Copyright (c) 2023 WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
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

public type Foo distinct isolated client object {
    public isolated function execute() returns anydata|error;
};

public type Bar distinct isolated client object {
    public isolated function execute() returns anydata|error;
};

public type Baz isolated client object {
    public isolated function execute() returns anydata|error;
};

public type Xyz distinct isolated service object {
    public isolated function execute() returns anydata|error;
};

public type Qux distinct isolated service object {
    public isolated function execute() returns anydata|error;
};

public type Quxx isolated client object {
    public isolated function execute() returns anydata|error;
};

public type Muxx isolated client object {
    public isolated function execute() returns anydata|error;
};
