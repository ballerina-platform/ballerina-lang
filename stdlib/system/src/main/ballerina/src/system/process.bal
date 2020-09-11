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
import ballerina/java;

# This object contains information on a process being created from Ballerina.
# This is returned from the `exec` function in the `system` module.
public class Process {

    private int BUF_SIZE = 10240;

# Waits for the process to finish its work and exit.
# ```ballerina
# int|error exitCode = process.waitForExit();
# ```
#
# + return - Returns the exit code for the process or else an `Error` if a failure occurs
    public function waitForExit() returns int|Error {
        return nativeWaitForExit(self);
    }

# Returns the exit code of the process when it has finished the execution.
# Error if the process has not exited yet.
# ```ballerina
# int|error exitCode = process.exitCode();
# ```
#
# + return - Returns the exit code of the process or else an `Error` if the process hasn't exited yet
    public function exitCode() returns int|Error {
        return nativeExitCode(self);
    }

# Destroys the process.
# ```ballerina
# process.destroy();
# ```
    public function destroy() {
        return nativeDestroy(self);
    }

# Provides a channel (to write into), which is made available as the 'standard input' for the process.
# ```ballerina
# io:WritableByteChannel output = process.stdin();
# ```
#
# + return - The `io:WritableByteChannel`, which represents the process's 'standard input'
    public function stdin() returns io:WritableByteChannel {
        return nativeStdin(self);
    }

# Provides a channel (to read from), which is made available as the 'standard output' of the process.
# ```ballerina
# io:ReadableByteChannel input = process.stdout();
# ```
#
# + return - The `io:ReadableByteChannel`, which represents the process's 'standard output'
    public function stdout() returns io:ReadableByteChannel {
        return nativeStdout(self);
    }

# Provides a channel (to read from), which is made available as the 'standard error' of the process.
# ```ballerina
# io:ReadableByteChannel input = process.stderr();
# ```
#
# + return - The `io:ReadableByteChannel`, which represents the process's 'standard error'
    public function stderr() returns io:ReadableByteChannel {
        return nativeStderr(self);
    }

# Pipes the standard output of the current process to the standard input of the given process.
# ```ballerina
# var x3out = x1.pipe(x2).pipe(x3).stdout();
# ```
#
# + process - The process to pipe the data to
# + return - The process that is passed to be used to help the chain pipe operations
    public function pipe(Process process) returns Process {
        io:ReadableByteChannel input = self.stdout();
        io:WritableByteChannel output = process.stdin();
        _ = @strand {thread: "any"} start self.doPipe(input, output);
        return process;
    }

    private function doPipe(io:ReadableByteChannel input, io:WritableByteChannel output) {
        while (true) {
            byte[] | io:Error result = input.read(self.BUF_SIZE);
            if (result is io:EofError) {
                break;
            } else if (result is io:Error) {
                io:println("Error in pipe read: ", result);
                break;
            } else {
                int i = 0;
                while (i < result.length()) {
                    var result2 = output.write(result, i);
                    if (result2 is error) {
                        io:println("Error in pipe write: ", result2);
                        break;
                    } else {
                        i = i + result2;
                    }
                }
            }
        }
        var cr1 = input.close();
        if (cr1 is error) {
            io:println("Error closing pipe input: ", cr1);
        }
        var cr2 = output.close();
        if (cr2 is error) {
            io:println("Error closing pipe output: ", cr2);
        }
    }

}

function nativeWaitForExit(Process process) returns int | Error = @java:Method {
    name: "waitForExit",
    'class: "org.ballerinalang.stdlib.system.nativeimpl.WaitForExit"
} external;

function nativeExitCode(Process process) returns int | Error = @java:Method {
    name: "exitCode",
    'class: "org.ballerinalang.stdlib.system.nativeimpl.ExitCode"
} external;

function nativeDestroy(Process process) = @java:Method {
    name: "destroy",
    'class: "org.ballerinalang.stdlib.system.nativeimpl.Destroy"
} external;

function nativeStdin(Process process) returns io:WritableByteChannel = @java:Method {
    name: "stdin",
    'class: "org.ballerinalang.stdlib.system.nativeimpl.Stdin"
} external;

function nativeStdout(Process process) returns io:ReadableByteChannel = @java:Method {
    name: "stdout",
    'class: "org.ballerinalang.stdlib.system.nativeimpl.Stdout"
} external;

function nativeStderr(Process process) returns io:ReadableByteChannel = @java:Method {
    name: "stderr",
    'class: "org.ballerinalang.stdlib.system.nativeimpl.Stderr"
} external;
