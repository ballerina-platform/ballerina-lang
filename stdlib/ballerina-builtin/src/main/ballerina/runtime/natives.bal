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

package ballerina.runtime;

@Description {value:"Halts the current worker for a predefined amount of time."}
@Param {value:"millis: Amount of time to sleep in milliseconds"}
public native function sleepCurrentWorker (int millis);

@Description {value:"Adds the given name, value pair to the system properties."}
@Param {value:"name: Name of the property"}
@Param {value:"value: Value of the property"}
public native function setProperty (@sensitive string name, string value);

@Description {value:"Returns the value associated with the specified property name."}
@Param {value:"name: Name of the property"}
@Return { value:"Value of the property if the property exists, an empty string otherwise"}
public native function getProperty (@sensitive string name) returns (string);

@Description {value:"Returns all system properties."}
@Return { value:"All system properties"}
public native function getProperties () returns (map);

@Description {value:"Returns the current working directory."}
@Return { value:"Current working directory or an empty string if the current working directory cannot be determined"}
public native function getCurrentDirectory () returns (string);

@Description {value:"Returns the charset encoding used in the runtime."}
@Return { value:"Encoding if it is available, an empty string otherwise"}
public native function getFileEncoding () returns (string);

@Description {value:"Executes a ballerina command and returns the input stream connected to the normal and error
output of the subprocess"}
@Param {value:"command: The command to be executed. It should be one of run, docker, build, install, uninstall, pull,
push, init, search, doc, grpc, swagger, test, version and encrypt"}
@Param {value:"packageName: Name of the package"}
@Return {value:"Data piped from the standard output and error output of the process"}
public native function execBallerina (string command, string packageName) returns (string);