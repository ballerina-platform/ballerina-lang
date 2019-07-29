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

import ballerina/io;

# This object contains information on a process being created from Ballerina.
# This is returned from the `exec` function in the `system` module.
public type Process object {

    # Waits for the process to finish its work and exit.
    #
    # + return - Returns the exist code for the process, or else an `Error` in a failure
    public function waitForExit() returns int|Error {
        return nativeWaitForExit(self);
    }
    
    # Returns the exit code of the process when it finished execution.
    # Error if the process has not exited yet.
    #
    # + return - Returns the exit code of the process, or else an `Error` if the process hasn't exited yet 
    public function exitCode() returns int|Error {
        return nativeExitCode(self);
    }
    
    # Destroys the process.
    public function destroy() {
        return nativeDestroy(self);
    }
    
    # Provides a channel to write into, where this data is made available as
    # the standard input for the process.
    #
    # + return - The `io:WritableByteChannel` representing the channel to write into for process's standard input
    public function stdin() returns io:WritableByteChannel {
        return nativeStdin(self);
    }
    
    # Provides a channel to read from, where this data as made available from
    # the standard output of the process.
    #
    # + return - The `io:ReadableByteChannel` representing the channel to read representing process's standard output
    public function stdout() returns io:ReadableByteChannel {
        return nativeStdout(self);
    }
    
    # Provides a channel to read from, where this data as made available from
    # the standard error of the process.
    #
    # + return - The `io:ReadableByteChannel` representing the channel to read representing process's standard error
    public function stderr() returns io:ReadableByteChannel {
        return nativeStderr(self);
    }

};

function nativeWaitForExit(Process process) returns int|Error = external;

function nativeExitCode(Process process) returns int|Error = external;

function nativeDestroy(Process process) = external;

function nativeStdin(Process process) returns io:WritableByteChannel = external;

function nativeStdout(Process process) returns io:ReadableByteChannel = external;

function nativeStderr(Process process) returns io:ReadableByteChannel = external;