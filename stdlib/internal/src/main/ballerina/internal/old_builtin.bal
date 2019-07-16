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

// Temporally moving old regex and string related functions to internal module.

public function equalsIgnoreCase(string src, string trg) returns boolean = external;

public function matches(string src, string regex) returns boolean = external;

public function replace(string src, string regex, string replaceWith) returns string = external;

public function replaceAll(string src, string regex, string replaceWith) returns string = external;

public function replaceFirst(string src, string regex, string replaceWith) returns string = external;

public function toByteArray(string src, string encoding) returns byte[] = external;

public function unescape(string src) returns string = external;

public function hasPrefix(string src, string prefix) returns boolean = external;

public function hasSuffix(string src, string suffix) returns boolean = external;

public function lastIndexOf(string src, string subString) returns int = external;

public function split(string src, string regex) returns string[] = external;

public function contains(string src, string subString) returns boolean = external;

public function toBoolean(string src) returns boolean = external;

public function hashCode(string src) returns int = external;
