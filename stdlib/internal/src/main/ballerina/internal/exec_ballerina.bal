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

# Executes a ballerina doc command
#
# + packageList - List of packages
# + outputPath - Path to write api-docs
# + templatesPath - Location of the templates
# + exclude - Packages to exclude
# + includeNatives - Generate docs for natives
# + envVars - Environment variables to pass
# + config - Path to configuration file
# + verbose - Verbose mode
# + sourceRoot - Root folder of the packages
# + return - Data piped from the standard output and error output of the process
public extern function execBallerinaDoc(string[] packageList,
                                        string? sourceRoot = (),
                                        string? outputPath = (),
                                        string? templatesPath = (),
                                        string[]? exclude = (),
                                        boolean includeNatives = false,
                                        map<string>? envVars = (),
                                        string? config = (),
                                        boolean verbose = false) returns (string|error);