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

# This object contains information on a process being created from Ballerina.
# This is returned from the `exec` function in the `system` module.
type Process object {

    # Waits for the process to finish its work and exit.
    #
    # + return - Returns the exist code for the process, or else an `Error` in a failure
    function waitForExit() returns int|Error {
        return nativeWaitForExit(self);
    }

};

function nativeWaitForExit(Process process) returns int|Error = external;