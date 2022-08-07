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

type AnnotationProcessor function (string name, function f) returns boolean|error;

AnnotationProcessor[] annotationProcessors = [
    processConfigAnnotation,
    processBeforeSuiteAnnotation,
    processAfterSuiteAnnotation,
    processBeforeEachAnnotation,
    processAfterEachAnnotation
];

function processAnnotation(string name, function f) returns error? {
    boolean annotationProcessed = false;
    foreach AnnotationProcessor annotationProcessor in annotationProcessors {
        if (check annotationProcessor(name, f)) {
            annotationProcessed = true;
            break;
        }
    }

    // Process the register functions under the test factory method.
    // Currently the dynamic registration does not support groups filtration.
    if !annotationProcessed && options.groups.length() == 0 && checkTest(name) {
        testRegistry.addFunction(name = name, testFunction = f);
    }
}

function processConfigAnnotation(string name, function f) returns boolean|error {
    TestConfig? config = (typeof f).@Config;
    if config != () {
        if !(options.groups.length() > 0 ? checkGroup(config.groups) : true && checkTest(name)) {
            return true;
        }
        if config.dataProvider != () {
            DataProviderReturnType|error params = trap config.dataProvider();
            if params is error {
                //TODO: append the data provider function's name if possible
                return error(string `Error while executing the dataProvider, ${params.message()}`);
            }
            testRegistry.addFunction(name = name, testFunction = f, params = params);
        }
        else if config.enable {
            testRegistry.addFunction(name = name, testFunction = f);
        }
        return true;
    }
    return false;
}

function processBeforeSuiteAnnotation(string name, function f) returns boolean|error {
    boolean? isTrue = (typeof f).@BeforeSuite;
    if isTrue == true {
        beforeSuiteRegistry.addFunction(name = name, testFunction = f);
        return true;
    }

    return false;

}

function processAfterSuiteAnnotation(string name, function f) returns boolean|error {
    AfterSuiteConfig? config = (typeof f).@AfterSuite;
    if config != () {
        afterSuiteRegistry.addFunction(name = name, testFunction = f);
        return true;
    }
    return false;
}

function processBeforeEachAnnotation(string name, function f) returns boolean|error {
    boolean? isTrue = (typeof f).@BeforeEach;
    if isTrue == true {
        beforeEachRegistry.addFunction(name = name, testFunction = f);
        return true;
    }
    return false;
}

function processAfterEachAnnotation(string name, function f) returns boolean|error {
    boolean? isTrue = (typeof f).@AfterEach;
    if isTrue == true {
        afterEachRegistry.addFunction(name = name, testFunction = f);
        return true;
    }
    return false;
}

function checkGroup(string[] groups) returns boolean {
    foreach string group in groups {
        if options.groups.indexOf(group) is int {
            return true;
        }
    }
    return false;
}

function checkTest(string name) returns boolean =>
    options.tests.length() > 0 ? (options.tests.indexOf(name) is int) : true;
