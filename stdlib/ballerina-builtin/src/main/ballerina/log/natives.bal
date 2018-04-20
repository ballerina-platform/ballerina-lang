// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package ballerina.log;

@Description {value: "Logs the specified value at DEBUG level."}
@Param {value: "msg: The message to be logged"}
public native function printDebug(string msg);

@Description {value: "Logs the specified message at ERROR level."}
@Param {value: "msg: The message to be logged"}
@Param {value: "err: The error struct to be logged"}
public native function printError(string msg, error? err = ());

@Description {value: "Logs the specified message at INFO level."}
@Param {value: "msg: The message to be logged."}
public native function printInfo(string msg);

@Description {value: "Logs the specified message at TRACE level."}
@Param {value: "msg: The message to be logged"}
public native function printTrace(string msg);

@Description {value: "Logs the specified message at WARN level."}
@Param {value: "msg: The message to be logged"}
public native function printWarn(string msg);