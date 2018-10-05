/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.ballerina.completion;

import io.ballerina.BallerinaSDKAware;
import io.ballerina.plugins.idea.project.BallerinaApplicationLibrariesService;

/**
 * SDK aware code completion test.
 */
@BallerinaSDKAware
public class BallerinaCompletionSDKAwareTest extends BallerinaCompletionTestBase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        BallerinaApplicationLibrariesService.getInstance().setLibraryRootUrls("temp:///");
        if (isSdkAware()) {
            setUpProjectSdk();
        }
    }

    @Override
    protected void tearDown() throws Exception {
        try {
            BallerinaApplicationLibrariesService.getInstance().setLibraryRootUrls();
        } finally {
            //noinspection ThrowFromFinallyBlock
            super.tearDown();
        }
    }

    public void testImportFromSDKFirstLevel() {
        doCheckResult("test.bal", "import bal<caret>", "import ballerina/", null);
    }

    public void testImportFromSDKSecondLevel() {
        doTest("import ballerina/<caret>", "auth", "builtin", "cache", "config", "crypto", "file", "grpc", "h2", "http",
                "internal", "io", "jdbc", "jms", "log", "math", "mb", "mime", "mysql", "observe", "reflect", "runtime",
                "sql", "streams", "swagger", "system", "task", "test", "time", "transactions", "websub");
    }

    public void testImportFromSDKAfterImport() {
        doCheckResult("test.bal", "import ballerina/http; import ba<caret>", "import ballerina/http; import ballerina/",
                null);
    }

    public void testImportFromSDKBeforeImport() {
        doCheckResult("test.bal", "import bal<caret> import ballerina.lang.system;",
                "import ballerina/ import ballerina.lang.system;", null);
    }

    public void testImportFromSDKBetweenImports() {
        doCheckResult("test.bal", "import ballerina/http; import bal<caret> import ballerina/math",
                "import ballerina/http; import ballerina/ import ballerina/math", null);
    }

    public void testPackageInvocationAutoCompletion() {
        doCheckResult("test.bal", "import ballerina/http; function test() {ht<caret>}",
                "import ballerina/http; function test() {http:}", null);
    }

    public void testFunctionInvocationFromAPackage() {
        doTestContains("import ballerina/log; function test() {log:<caret>}", "printDebug");
    }

    public void testFunctionInvocationFromAPackageAutoCompletion() {
        doCheckResult("test.bal", "import ballerina/log; function test() {log:printD<caret>}",
                "import ballerina/log; function test() {log:printDebug();}", null);
    }

    public void testMultiLevelFunctionInvocation1() {
        doTestContains("function test() {string s =\"\"; \n    string s2 = s.<caret>}", "contains", "equalsIgnoreCase",
                "hasPrefix", "hasSuffix", "indexOf", "lastIndexOf", "length", "replace", "replaceAll", "replaceFirst",
                "split", "substring", "toByteArray", "toLower", "toUpper", "trim", "unescape");
    }


    public void testMultiLevelFunctionInvocation2() {
        doTestContains("function test() {string s =\"\"; \n    string s2 = s.toUpper().<caret>}", "contains",
                "equalsIgnoreCase", "hasPrefix", "hasSuffix", "indexOf", "lastIndexOf", "length", "replace",
                "replaceAll", "replaceFirst", "split", "substring", "toByteArray", "toLower", "toUpper", "trim",
                "unescape");
    }

    public void testMultiLevelFunctionInvocation3() {
        doTestContains("function test() {string s =\"\"; \n    string s2 = s.toUpper().toLower().<caret>}",
                "contains",
                "equalsIgnoreCase", "hasPrefix", "hasSuffix", "indexOf", "lastIndexOf", "length", "replace",
                "replaceAll", "replaceFirst", "split", "substring", "toByteArray", "toLower", "toUpper", "trim",
                "unescape");
    }
}
