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
# + dataProvider - Function, which will be used to feed data into this test
# + before - Function to be run before the test is run
# + after - Function to be run after the test is run
# + dependsOn - A list of functions the test function depends on and will be run before the test
public type TestConfig record {
    boolean enable = true;
    string[] groups = [];
    function () returns DataProviderReturnType dataProvider?;
    function () returns (any|error) before?;
    function () returns (any|error) after?;
    function[] dependsOn = [];
};

# Configuration of the function to be mocked.
#
# + moduleName - Name of the module, which includes the function to be mocked
# + functionName - Name of the function to be mocked
public type MockConfig record {
    string moduleName = ".";
    string functionName = "";
};

# Configuration set for AfterSuite functions.
#
# + alwaysRun - Flag to indicate whether the afterSuite function needs to be executed irrespective of other dependent functions
public type AfterSuiteConfig record {
    boolean alwaysRun = false;
};

# Configuration set for BeforeGroups functions.
#
# + value - List of groups before which the beforeGroups function needs to be executed
public type BeforeGroupsConfig record {
    string[] value = [];
};

# Configuration set for AfterGroups functions.
#
# + value - List of groups after which the afterGroups function needs to be executed
# + alwaysRun - Flag to indicate whether the afterGroups function needs to be executed irrespective of other dependent functions
public type AfterGroupsConfig record {
    string[] value = [];
    boolean alwaysRun = false;
};

# Identifies test function.
public annotation TestConfig Config on function;

//TODO: Enable dynamic registration upon approval
# Identifies test factory function for dynamic test registration.
// public annotation Factory on function;

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
