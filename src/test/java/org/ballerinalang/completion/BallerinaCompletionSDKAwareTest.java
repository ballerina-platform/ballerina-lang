/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.completion;

import org.ballerinalang.BallerinaSDKAware;
import org.ballerinalang.plugins.idea.project.BallerinaApplicationLibrariesService;

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
        doTest("import <caret>", "ballerina");
    }

    public void testImportFromSDKSecondLevel() {
        doTest("import ballerina.<caret>", "data", "doc", "lang", "net", "utils");
    }

    public void testImportFromSDKThirdLevel() {
        doTest("import ballerina.lang.<caret>", "arrays", "blobs", "datatables", "errors", "files", "jsons", "maps",
                "messages", "strings", "system", "type", "xmls");
    }

    public void testImportFromSDKAfterPackage() {
        doTest("package test; import <caret>", "ballerina");
    }

    public void testImportFromSDKAfterImport() {
        doTest("package test; import ballerina.lang.system; import <caret>", "ballerina");
    }

    public void testImportFromSDKBeforeImport() {
        doTest("package test; import <caret> import ballerina.lang.system;", "ballerina");
    }

    public void testImportFromSDKBetweenImports() {
        doTest("package test; import ballerina.lang.system; import <caret> import ballerina.utils;", "ballerina");
    }

    public void testPackageInvocationAutoCompletion() {
        doCheckResult("test.bal", "import ballerina.lang.system; function test() {sys<caret>}",
                "import ballerina.lang.system; function test() {system:}", null);
    }

    public void testFunctionInvocationFromAPackage() {
        doTest("import ballerina.lang.system; function test() {system:<caret>}", "currentTimeMillis", "epochTime",
                "getDateFormat", "getEnv", "log", "nanoTime", "print", "println");
    }

    public void testFunctionInvocationFromAPackageAutoCompletion() {
        doCheckResult("test.bal", "import ballerina.lang.system; function test() {system:nano<caret>}",
                "import ballerina.lang.system; function test() {system:nanoTime()}", null);
    }
}
