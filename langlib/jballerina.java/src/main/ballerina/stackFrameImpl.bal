// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Implementation for the `runtime.StackFrame`.
#
# + callableName - Callable name
# + moduleName - Module name
# + fileName - File name
# + lineNumber - Line number
public readonly class StackFrameImpl {

    public string callableName;
    public string? moduleName;
    public string fileName;
    public int lineNumber;

    # Returns a string representing for the `StackFrame`.
    #
    # + return - A stack frame as string
    public isolated function toString() returns string {
        var moduleName = self.moduleName;
        if (moduleName is string) {
            return "callableName: " + self.callableName + " " + "moduleName: " + moduleName +
                            " " + "fileName: " + self.fileName + " " + "lineNumber: " + getStringValue(self.lineNumber);
        } else {
            return "callableName: " + self.callableName + " " +
                            " " + "fileName: " + self.fileName + " " + "lineNumber: " + getStringValue(self.lineNumber);
        }

    }

    public isolated function init(string callableName, string fileName, int lineNumber, string? moduleName) {
        self.callableName = callableName;
        self.moduleName = moduleName;
        self.fileName = fileName;
        self.lineNumber = lineNumber;
    }
}
