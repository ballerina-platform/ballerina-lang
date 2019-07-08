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

@typeParam
type RecordType record {|
    string message?;
    error cause?;
    (anydata|error)...;
|};

@typeParam
type StringType string;

// Returns the error's reason string
public function reason(error<StringType> e) returns StringType = external;

// Returns the error's detail record as a frozen mapping
public function detail(error<string,RecordType> e) returns RecordType = external;

// Returns an object representing the stack trace of the error
public function stackTrace(error e) returns object { } = external;


public type Detail record {|
    string message?;
    error cause?;
    (anydata|error)...;
|};