// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//  http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

# Object Test
# Description.
# + url - endpoint url
# + path - a valid path
public type Test object {
    public string url;
    public string path;
    private string idx;

    # Initialized a new `Test` object
    # + abc - This is abc
    # + path - This is path
    public function __init(string abc = "abc", string path = "def") {}

    # test1 function
    # + x - an integer
    # + return - is success?
    public function test1(int x) returns boolean { return true; }

    # test1 function
    # + return - returns the string or an error
    public function test2() returns string|error { return "hello"; }
};
