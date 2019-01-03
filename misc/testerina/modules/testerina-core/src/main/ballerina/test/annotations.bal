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

# Configuration set for test functions.
#
# + enable - Flag to enable/disable test functions
# + groups - List of groups that this test function belongs to
# + dataProvider - Name of the function which will be used to feed data into this test
# + before - Name of the function to be run before the test is run
# + after - Name of the function to be run after the test is run
# + dependsOn - A list of function names the test function depends on, and will be run before the test
public type TestConfig record {
    boolean enable = true;
    string[] groups = [];
    string dataProvider = "";
    string before = "";
    string after = "";
    string[] dependsOn = [];
};

# Configuration of the function to be mocked.
#
# + moduleName - Name of the module that the function to be mocked resides in
# + functionName - Name of the function to be mocked
public type MockConfig record {
    string moduleName = ".";
    string functionName = "";
};

public annotation<function> Config TestConfig;

public annotation<function> Mock MockConfig;

# Identifies beforeSuite function.
public annotation<function> BeforeSuite;

# Identifies afterSuite function.
public annotation<function> AfterSuite;

# Identifies beforeTest function.
public annotation<function> BeforeEach;

# Identifies afterTest function.
public annotation<function> AfterEach;
