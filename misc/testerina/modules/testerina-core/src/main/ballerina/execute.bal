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
import ballerina/lang.'error as langError;
import ballerina/lang.runtime;

isolated boolean shouldSkip = false;
boolean shouldAfterSuiteSkip = false;
isolated int exitCode = 0;
isolated final ConcurrentExecutionManager conMgr = new ();

public function startSuite() returns int {
    // exit if setTestOptions has failed
    lock {
        if exitCode > 0 {
            return exitCode;
        }
    }

    if listGroups {
        string[] groupsList = groupStatusRegistry.getGroupsList();
        if groupsList.length() == 0 {
            println("\tThere are no groups available!");
        } else {
            println("\tFollowing groups are available :");
            println("\t[" + string:'join(", ", ...groupsList) + "]");
        }
    } else {
        if testRegistry.getFunctions().length() == 0 && testRegistry.getDependentFunctions().length() == 0 {
            println("\tNo tests found");
            lock {
                return exitCode;
            }

        }

        error? err = orderTests();
        if err is error {
            enableExit();
            println(err.message());
        } else {
            executeBeforeSuiteFunctions();
            err = executeTests();
            if err is error {
                enableExit();
                println(err.message());
            }
            executeAfterSuiteFunctions();
            reportGenerators.forEach(reportGen => reportGen(reportData));
        }
    }
    lock {
        return exitCode;
    }
}

function executeTests() returns error? {
    decimal startTime = currentTimeInMillis();
    foreach TestFunction testFunction in testRegistry.getFunctions() {
        _ = testFunction.parallelizable ? conMgr.addInitialParallelTest(testFunction) : conMgr.addInitialSerialTest(testFunction);
    }

    while !conMgr.isExecutionDone() {

        if conMgr.getAvailableWorkers() != 0 {
            conMgr.waitUntilEmptyQueueFilled();

            if conMgr.getSerialQueueLength() != 0 && conMgr.getAvailableWorkers() == conMgr.getConfiguredWorkers() {
                TestFunction testFunction = conMgr.getSerialTest();
                conMgr.addTestInExecution(testFunction);
                conMgr.allocateWorker();
                future<error?> serialWaiter = start executeTest(testFunction);
                any _ = check wait serialWaiter;

            }

            else if conMgr.getParallelQueueLength() != 0 && conMgr.getSerialQueueLength() == 0 {
                TestFunction testFunction = conMgr.getParallelTest();
                conMgr.addTestInExecution(testFunction);
                conMgr.allocateWorker();
                future<(error?)> parallelWaiter = start executeTest(testFunction);
                if isDataDrivenTest(testFunction) {
                    any _ = check wait parallelWaiter;
                }

            }

        }
        runtime:sleep(0.0001); // sleep is added to yield the strand
    }

    println("\n\t\tTest execution time :" + (currentTimeInMillis() - startTime).toString() + "ms\n");
}

function executeBeforeSuiteFunctions() {
    ExecutionError? err = executeFunctions(beforeSuiteRegistry.getFunctions());
    if err is ExecutionError {
        enableShouldSkip();
        shouldAfterSuiteSkip = true;
        enableExit();
        printExecutionError(err, "before test suite function");
    }
}

function executeAfterSuiteFunctions() {
    ExecutionError? err = executeFunctions(afterSuiteRegistry.getFunctions(), shouldAfterSuiteSkip);
    if err is ExecutionError {
        enableExit();
        printExecutionError(err, "after test suite function");
    }
}

function orderTests() returns error? {
    string[] descendants = [];

    foreach TestFunction testFunction in testRegistry.getDependentFunctions() {
        if !conMgr.isVisited(testFunction.name) && conMgr.isEnabled(testFunction.name) {
            check restructureTest(testFunction, descendants);
        }
    }
}

function restructureTest(TestFunction testFunction, string[] descendants) returns error? {
    descendants.push(testFunction.name);

    foreach function dependsOnFunction in testFunction.dependsOn {
        TestFunction dependsOnTestFunction = check testRegistry.getTestFunction(dependsOnFunction);

        // if the dependsOnFunction is disabled by the user, throw an error
        // dependsOnTestFunction.config?.enable is used instead of dependsOnTestFunction.enable to ensure that
        // the user has deliberately passed enable=false
        boolean? dependentEnabled = dependsOnTestFunction.config?.enable;
        if dependentEnabled != () && !dependentEnabled {
            string errMsg = string `error: Test [${testFunction.name}] depends on function [${dependsOnTestFunction.name}], `
            + string `but it is either disabled or not included.`;
            return error(errMsg);
        }

        conMgr.addDependent(dependsOnTestFunction.name, testFunction);

        // Contains cyclic dependencies
        int? startIndex = descendants.indexOf(dependsOnTestFunction.name);
        if startIndex is int {
            string[] newCycle = descendants.slice(startIndex);
            newCycle.push(dependsOnTestFunction.name);
            return error("Cyclic test dependencies detected: " + string:'join(" -> ", ...newCycle));
        } else if !conMgr.isVisited(dependsOnTestFunction.name) {
            check restructureTest(dependsOnTestFunction, descendants);
        }
    }

    conMgr.setEnabled(testFunction.name);
    conMgr.setVisited(testFunction.name);
    _ = descendants.pop();
}

isolated function printExecutionError(ExecutionError err, string functionSuffix)
    => println("\t[fail] " + err.detail().functionName + "[" + functionSuffix + "]" + ":\n\t    " + formatFailedError(err.message(), 2));

isolated function getErrorMessage(error err) returns string {
    string message = err.toBalString();

    string accumulatedTrace = "";
    foreach langError:StackFrame stackFrame in err.stackTrace() {
        accumulatedTrace = accumulatedTrace + "\t" + stackFrame.toString() + "\n";
    }
    return message + "\n" + accumulatedTrace;
}

isolated function getTestType(TestFunction testFunction) returns TestType {
    DataProviderReturnType? params = testFunction.params;
    if (params is map<AnyOrError[]>) {
        return DATA_DRIVEN_MAP_OF_TUPLE;
    } else if (params is AnyOrError[][]) {
        return DATA_DRIVEN_TUPLE_OF_TUPLE;
    }
    return GENERAL_TEST;
}

isolated function nestedEnabledDependentsAvailable(TestFunction[] dependents) returns boolean {
    if (dependents.length() == 0) {
        return false;
    }
    TestFunction[] queue = [];
    foreach TestFunction dependent in dependents {
        if (conMgr.isEnabled(dependent.name)) {
            return true;
        }
        foreach TestFunction superDependent in conMgr.getDependents(dependent.name) {
            queue.push(superDependent);
        }
    }
    return nestedEnabledDependentsAvailable(queue);
}

isolated function isDataDrivenTest(TestFunction testFunction) returns boolean =>
        testFunction.params is map<AnyOrError[]> || testFunction.params is AnyOrError[][];

isolated function enableShouldSkip() {
    lock {
        shouldSkip = true;
    }
}

isolated function getShouldSkip() returns boolean {
    lock {
        return shouldSkip;
    }
}

isolated function enableExit() {
    lock {
        exitCode = 1;
    }
}

