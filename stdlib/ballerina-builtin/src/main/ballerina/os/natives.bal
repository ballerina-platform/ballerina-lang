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

@Description { value:"Execute a given CMD command or a script"}
@Param {value:"command: Command to be executed"}
@Param {value:"workingDir: Execution workspace, the command will be executed from here"}
@Param {value:"timeoutValue: Timeout value for the process to exit, if this is exceeded the process is killed"}
@Return { value:" Returns a error if the execution is not successfull or user timeout exceeds"}
public native function runCommand(@sensitive string command, string workingDir = "", int timeoutValue = -1)
                                                                                                    returns error|();
