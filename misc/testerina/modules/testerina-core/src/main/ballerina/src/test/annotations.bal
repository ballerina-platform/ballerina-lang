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
# + moduleName - Name of the module of the function to be mocked
# + functionName - Name of the function to be mocked
public type MockConfig record {
    string moduleName = ".";
    string functionName = "";
};

public type AfterSuiteConfig record {
    boolean alwaysRun = false;
};

public type BeforeGroupsConfig record {
    string[] value = [];
};

public type AfterGroupsConfig record {
    string[] value = [];
};

public annotation TestConfig Config on function;

# Identifies beforeSuite function.
public annotation BeforeSuite on function;

# Identifies afterSuite function.
public annotation AfterSuiteConfig AfterSuite on function;

# Identifies beforeGroup function.
public annotation BeforeGroupsConfig BeforeGroups on function;

# Identifies afterGroup function.
public annotation AfterGroupsConfig AfterGroups on function;

# Identifies beforeTest function.
public annotation BeforeEach on function;

# Identifies afterTest function.
public annotation AfterEach on function;

# Identifies the MockFunction object
public const annotation MockConfig Mock on source var, function;
