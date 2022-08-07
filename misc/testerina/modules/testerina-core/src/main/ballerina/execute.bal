// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

boolean shouldSkip = false;
boolean shouldAfterSuiteSkip = false;
boolean shouldAfterEachSkip = false;
map<boolean> shouldAfterGroupSkip = {};

enum TestLifeCycle {
    BEFORE_SUITE,
    BEFORE_GROUP,
    BEFORE_EACH,
    AFTER_EACH,
    AFTER_GROUP,
    AFTER_SUITE
}

public function execute() returns map<error?> {
    executeFunctions(beforeSuiteRegistry, BEFORE_SUITE);
    map<error?> result = executeTests();
    executeFunctions(afterSuiteRegistry, AFTER_SUITE);

    return result;
}

function executeFunctions(TestRegistry registry, TestLifeCycle lifeCycle) {
    foreach TestFunction testFunction in registry.getFunctions() {
        if testFunction.params != () {
            //TODO: generate an error
        } else {
            any|error output = trap function:call(testFunction.testFunction);
            if output is error { //TODO: return the error
    
            }
        }
    }
}

function executeTests() returns map<error?> {
    map<error?> result = {};
    foreach TestFunction testFunction in testRegistry.getFunctions() {
        executeFunctions(beforeEachRegistry, BEFORE_EACH);

        DataProviderReturnType? params = testFunction.params;
        if params is map<any[]> {
            foreach [string, any[]] entry in params.entries() {
                executeFunction(result, testFunction.testFunction, testFunction.name + "#" + entry[0], entry[1]);
            }
        } else if params is any[][] {
            int i = 0;
            foreach any[] entry in params {
                executeFunction(result, testFunction.testFunction, testFunction.name + "#" + i.toString(), entry);
                i += 1;
            }
        } else {
            executeFunction(result, testFunction.testFunction, testFunction.name);
        }

        executeFunctions(afterEachRegistry, AFTER_EACH);
    }
    return result;
}

function executeFunction(map<error?> result, function testFunction, string name, (any|error)[]? params = ()) {
    any|error output = params == () ? trap function:call  (testFunction) : trap function:call  (testFunction, ... params);
    if output is error {
        onFailed(name, output);
    } else {
        onPassed(name);
    }
}
