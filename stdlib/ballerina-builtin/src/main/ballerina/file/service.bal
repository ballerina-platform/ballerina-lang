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

package ballerina.file;

@Description {value:"Represents an event which will trigger when there is a changes to listining direcotry"}
@Field {value:"name: Absolute file URI for triggerd event"}
@Field {value:"operation: Triggered event action. This can be create, delete or modify"}
public type FileEvent {
    string name,
    string operation,
};

@Description {value:"Represents an error which will occur while DirectoryListener operations"}
@Field {value:"message:  An error message explaining about the error"}
@Field {value:"cause: The error(s) that caused FSError to get thrown"}
public type FSError {
    string message,
    error[] cause,
};
