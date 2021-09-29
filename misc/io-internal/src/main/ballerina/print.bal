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

import ballerina/jballerina.java;

// Replicated from https://github.com/ballerina-platform/module-ballerina-io/blob/master/ballerina/print.bal#L23
public type Printable any|error|PrintableRawTemplate;

// Replicated from https://github.com/ballerina-platform/module-ballerina-io/blob/master/ballerina/print.bal#L34
public type PrintableRawTemplate object {
    public string[] & readonly strings;
    public Printable[] insertions;
};

# Prints `any`, `error` or string templates(such as `The respective int value is ${val}`) value(s)
# to the STDOUT followed by a new line.
#
# + values - The value(s) to be printed
public isolated function println(Printable... values) {
    string value = "";
    foreach Printable printableValue in values {
        PrintableClassImpl printable = new PrintableClassImpl(printableValue);
        value = value.concat(printable.toString());
    }

    externPrintln(out(), java:fromString(value));
}

// Replicated from https://github.com/ballerina-platform/module-ballerina-io/blob/master/ballerina/print.bal#L98
class PrintableClassImpl {
    Printable printable;

    public isolated function init(Printable printable) {
        self.printable = printable;
    }
    public isolated function toString() returns string {
        Printable printable = self.printable;
        if printable is PrintableRawTemplate {
            return new PrintableRawTemplateImpl(printable).toString();
        } else if printable is error {
            return printable.toString();
        } else {
            return printable.toString();
        }
    }
}

// Replicated from https://github.com/ballerina-platform/module-ballerina-io/blob/master/ballerina/print.bal#L116
class PrintableRawTemplateImpl {
    *object:RawTemplate;
    public Printable[] insertions;

    public isolated function init(PrintableRawTemplate printableRawTemplate) {
        self.strings = printableRawTemplate.strings;
        self.insertions = printableRawTemplate.insertions;
    }

    public isolated function toString() returns string {
        Printable[] templeteInsertions = self.insertions;
        string[] templeteStrings = self.strings;
        string templatedString = templeteStrings[0];
        foreach int i in 1 ..< (templeteStrings.length()) {
            Printable templateInsert = templeteInsertions[i - 1];
            if (templateInsert is PrintableRawTemplate) {
                templatedString += new PrintableRawTemplateImpl(templateInsert).toString() + templeteStrings[i];
            } else if (templateInsert is error) {
                templatedString += templateInsert.toString() + templeteStrings[i];
            } else {
                templatedString += templateInsert.toString() + templeteStrings[i];
            }
        }
        return templatedString;
    }
}

# Retrieves the current system output stream.
#
# + return - The current system output stream
isolated function out() returns handle = @java:FieldGet {
    name: "out",
    'class: "java.lang.System"
} external;

# Calls `println` method of the `PrintStream`.
#
# + receiver - The `handle`, which refers to the current system output stream
# + message - The `handle`, which refers to the Java String representation of the Ballerina `string`
isolated function externPrintln(handle receiver, handle message) = @java:Method {
    paramTypes: ["java.lang.String"],
    'class: "java.io.PrintStream",
    name: "println"
} external;
