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

public type BallerinaCommand "run" | "docker" | "build" | "install" | "uninstall" | "pull" | "push" | "init" |
"search" | "doc" | "grpc" | "swagger" | "test" | "version" | "encrypt";

documentation{
    Executes a ballerina command
    P{{command}} - Ballerina command
    P{{packagePath}} - Package path with necessary flags
    R{{}} - Data piped from the standard output and error output of the process
}
public native function execBallerina(@sensitive BallerinaCommand command, @sensitive string packagePath)
    returns (string|error);