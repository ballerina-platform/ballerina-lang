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

import ballerinax/java;

# Splits a string around matches of the given delimiter.
#
# + receiver - original string
# + delimiter - delimiter
# + return - array of strings
public function split(string receiver, string delimiter) returns string[] {
    handle res = splitExternal(java:fromString(receiver), java:fromString(delimiter));
    return getBallerinaStringArray(res);
}

# Replaces each substring of this string that matches the literal target sequence with the specified literal
# replacement sequence.
#
# + originalText - original string
# + textToReplace - string to replace
# + replacement - replacement string
# + return - the resultant string
public function replace(string originalText, string textToReplace, string replacement) returns string {
    return replaceExternal(java:fromString(originalText), java:fromString(textToReplace), java:fromString(replacement));
}

// Interoperable external functions.
function splitExternal(handle receiver, handle delimiter) returns handle = @java:Method {
    name: "split",
    class: "java.lang.String",
    paramTypes: ["java.lang.String"]
} external;

function replaceExternal(handle originalText, handle textToReplace, handle replacement) returns string = @java:Method {
    name: "replace",
    class: "java.lang.String",
    paramTypes: ["java.lang.String", "java.lang.String"]
} external;

function getBallerinaStringArray(handle h) returns string[] = @java:Constructor {
    class:"org/ballerinalang/jvm/values/ArrayValue",
    paramTypes:["[Ljava.lang.String;"]
} external;
