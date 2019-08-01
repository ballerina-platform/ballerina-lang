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

    private int BUF_SIZE = 10240;

    # Waits for the process to finish its work and exit.
    #
    # + return - Returns the exit code for the process, or an `Error` if a failure occurs
    public function waitForExit() returns int|Error {
        return nativeWaitForExit(self);
    }
    
    # Returns the exit code of the process when it finished execution.
    #
    # + return - Returns the exit code of the process, or an `Error` if the process hasn't exited yet.
    public function exitCode() returns int|Error {
        return nativeExitCode(self);
    }
    
    # Destroys the process.
    public function destroy() {
        return nativeDestroy(self);
    }
    
    # Provides a channel (to write into), which is made available as the 'standard input' for the process.
    #
    # + return - The `io:WritableByteChannel` which represents the process's 'standard input'
    public function stdin() returns io:WritableByteChannel {
        return nativeStdin(self);
    }
    
    # Provides a channel (to read from), which is made available as the 'standard output' of the process.
    #
    # + return - The `io:ReadableByteChannel` which represents the process's 'standard output'
    public function stdout() returns io:ReadableByteChannel {
        return nativeStdout(self);
    }
    
    # Provides a channel (to read from), which is made available as the 'standard error' of the process.
    #
    # + return - The `io:ReadableByteChannel` which represents the process's 'standard error'
    public function stderr() returns io:ReadableByteChannel {
        return nativeStderr(self);
    }
    
    # Pipes the standard output of the current process to the standard input of the given process.
    #
    # + process - The process to pipe the data to
    # + return - The process that is passed in, which is used to help chain pipe operations
    public function pipe(Process process) returns Process {
        _ = start self.doPipe(self.stdout(), process.stdin());
        return process;
    }
    
    private function doPipe(io:ReadableByteChannel input, io:WritableByteChannel output) {
        while (true) {
            [byte[], int]|error result = input.read(self.BUF_SIZE);
            if (result is error) {
                io:println("Error in pipe read: ", result);
                break;
            } else {
                if (result[1] <= 0) {
                    break;
                } else {
                    int i = 0;
                    while (i < result[1]) {
                        var result2 = output.write(result[0], i);
                        if (result2 is error) {
                            io:println("Error in pipe write: ", result2);
                            break;
                        } else {
                            i = i + result2;
                        }
                    }
                }
            }
        }
        
        var cr2 = input.close();
        if (cr2 is error) {
            io:println("Error closing pipe input: ", cr2);
        }
        var cr1 = output.close();
        if (cr1 is error) {
            io:println("Error closing pipe output: ", cr1);
        }
        
    }

};

function nativeWaitForExit(Process process) returns int|Error = external;

function nativeDestroy(Process process) = external;

function nativeStdin(Process process) returns io:WritableByteChannel = external;

function nativeStdout(Process process) returns io:ReadableByteChannel = external;

function nativeStderr(Process process) returns io:ReadableByteChannel = external;
