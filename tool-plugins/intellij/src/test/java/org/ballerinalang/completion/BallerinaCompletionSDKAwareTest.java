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

    //    public void testImportFromSDKFirstLevel() {
    //        doTest("import <caret>", "ballerina");
    //    }

    public void testImportFromSDKSecondLevel() {
        doTest("import ballerina.<caret>", "builtin", "caching", "config", "data", "file", "io", "log", "math",
                "net", "os", "runtime", "security", "task", "user", "util");
    }

    public void testImportFromSDKThirdLevel() {
        doTest("import ballerina.net.<caret>", "http", "uri", "ws");
    }

    //    public void testImportFromSDKAfterPackage() {
    //        doTest("package test; import <caret>", "ballerina");
    //    }

    //    public void testImportFromSDKAfterImport() {
    //        doTest("package test; import ballerina.lang.system; import <caret>", "ballerina");
    //    }

    //    public void testImportFromSDKBeforeImport() {
    //        doTest("package test; import <caret> import ballerina.lang.system;", "ballerina");
    //    }

    //    public void testImportFromSDKBetweenImports() {
    //        doTest("package test; import ballerina.lang.system; import <caret> import ballerina.utils;", "ballerina");
    //    }

    //    public void testPackageInvocationAutoCompletion() {
    //        doCheckResult("test.bal", "import ballerina.net.http; function test() {ht<caret>}",
    //                "import ballerina.net.http; function test() {http:}", null);
    //    }
    //
    //    public void testFunctionInvocationFromAPackage() {
    //        doTest("import ballerina.net.uri; function test() {uri:<caret>}", "encode");
    //    }
    //
    //    public void testFunctionInvocationFromAPackageAutoCompletion() {
    //        doCheckResult("test.bal", "import ballerina.net.uri; function test() {uri:en<caret>}",
    //                "import ballerina.net.uri; function test() {uri:encode()}", null);
    //    }
    //
    //    public void testMultiLevelFunctionInvocation1() {
    //        doTest("function test() {string s =\"\"; \n    string s2 = s.<caret>}", "contains", "equalsIgnoreCase",
    //                "hasPrefix", "hasSuffix", "indexOf", "lastIndexOf", "length", "replace", "replaceAll",
    //                "replaceFirst", "split", "subString", "toBlob", "toLowerCase", "toUpperCase", "trim", "unescape");
    //    }
    //
    //    public void testMultiLevelFunctionInvocation2() {
    //        doTest("function test() {string s =\"\"; \n    string s2 = s.toUpperCase().<caret>}", "contains",
    //                "equalsIgnoreCase", "hasPrefix", "hasSuffix", "indexOf", "lastIndexOf", "length", "replace",
    //                "replaceAll", "replaceFirst", "split", "subString", "toBlob", "toLowerCase", "toUpperCase",
    // "trim",
    //                "unescape");
    //    }
    //
    //    public void testMultiLevelFunctionInvocation3() {
    //        doTest("function test() {string s =\"\"; \n    string s2 = s.toUpperCase().toLowerCase().<caret>}",
    // "contains",
    //                "equalsIgnoreCase", "hasPrefix", "hasSuffix", "indexOf", "lastIndexOf", "length", "replace",
    //                "replaceAll", "replaceFirst", "split", "subString", "toBlob", "toLowerCase", "toUpperCase",
    // "trim",
    //                "unescape");
    //    }
    //
    //    public void testMultiLevelFunctionInvocation4() {
    //        doTest("function test() {string s =\"\"; \n    string s2 = s.toUpperCase().toBlob().<caret>}",
    // "toString");
    //    }
}
