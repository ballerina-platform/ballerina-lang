// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/file;

listener file:Listener localFolder = new ({
    path: "src/test/resources/fs",
    recursive: false
});

boolean createInvoke = false;
boolean modifyInvoke = false;
boolean deleteInvoke = false;

service fileSystem on localFolder {

    resource function onCreate(file:FileEvent m) {
        createInvoke = true;
    }

    resource function onModify(file:FileEvent m) {
        modifyInvoke = true;
    }

    resource function onDelete(file:FileEvent m) {
        deleteInvoke = true;
    }
}

function isCreateInvoked() returns boolean {
    return createInvoke;
}

function isModifyInvoked() returns boolean {
    return modifyInvoke;
}

function isDeleteInvoked() returns boolean {
    return deleteInvoke;
}
