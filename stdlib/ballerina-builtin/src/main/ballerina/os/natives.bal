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

package ballerina.os;

@Description {value:"Returns the environment variable value associated with the provided name."}
@Param {value:"name: Name of the environment variable"}
@Return { value:"Environment variable value if it exists, otherwise an empty string"}
public native function getEnv (@sensitive string name) returns (string);

@Description {value:"Splits the value of an environment variable using path separator and returns the separated values as an array."}
@Param {value:"name: Name of the environment variable"}
@Return { value:"Environment variable values as an array if the provided environment variable exists, otherwise an empty array"}
public native function getMultivaluedEnv (@sensitive string name) returns (string[]);

@Description {value:"Returns the name of the operating system."}
@Return { value:"OS name if the OS can be identified, an empty string otherwise"}
public native function getName () returns (string);

@Description {value:"Returns the version of the operating system."}
@Return { value:"OS version if the OS can be identified, an empty string otherwise"}
public native function getVersion () returns (string);

@Description {value:"Returns the architecture of the operating system."}
@Return { value:"OS architecture if the OS can be identified, an empty string otherwise"}
public native function getArchitecture () returns (string);


public function runCommand (string command, string workspace = "", int timeout1 = -1)
                                                (CommandExecutionError cErr, GenericError err){
    cErr, err = runCommandNative (command, workspace, timeout1);
    return;
}

@Description { value:"Execute a given CMD command or a script"}
@Return { value:" Returns 1 if the execution is successfull, 0 otherwise"}
public native function runCommandNative (string command, string workspace, int timeout1) (CommandExecutionError, GenericError);

@Description { value: "Represents a error which could occur when executing a command"}
@Field { value : "message: The error message"}
@Field { value : "cause: The error cause"}
public struct CommandExecutionError {
    string message;
    error[] cause;
}

@Description { value: "Represents an error which could occur if process id interrupted."}
@Field { value : "message: The error message"}
@Field { value : "cause: The caused of the error"}
public struct GenericError {
    string message;
    error[] cause;
}
