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

package org.ballerinalang.editor;

import org.ballerinalang.BallerinaCodeInsightFixtureTestCase;

/**
 * Live template tests.
 */
public class BallerinaLiveTemplateTest extends BallerinaCodeInsightFixtureTestCase {

    //    public void testMainFunction() {
    //        myFixture.configureByText("test.bal", "<caret>");
    //        myFixture.type("funm\t");
    //        myFixture.checkResult("function main (string[] args) {\n    \n}");
    //    }
    //
    //    public void testFunctionInEmptyFile() {
    //        myFixture.configureByText("test.bal", "<caret>");
    //        myFixture.type("fun\t");
    //        myFixture.checkResult("function  ()  {\n    \n}");
    //    }
    //
    //    public void testFunctionAfterFunction() {
    //        myFixture.configureByText("test.bal", "function test(){\n\n}\n<caret>");
    //        myFixture.type("fun\t");
    //        myFixture.checkResult("function test(){\n\n}\nfunction  ()  {\n    \n}");
    //    }
    //
    //    public void testFunctionBeforeFunction() {
    //        myFixture.configureByText("test.bal", "<caret>\nfunction test(){\n\n}\n");
    //        myFixture.type("fun\t");
    //        myFixture.checkResult("function  ()  {\n    \n}\nfunction test(){\n\n}\n");
    //    }
    //
    //    public void testServiceInEmptyFile() {
    //        myFixture.configureByText("test.bal", "<caret>");
    //        myFixture.type("ser\t");
    //        myFixture.checkResult("\nservice<>  {\n    \n}");
    //    }
    //
    //    public void testServiceAfterService() {
    //        myFixture.configureByText("test.bal", "service<http> test {\n    \n}\n<caret>");
    //        myFixture.type("ser\t");
    //        myFixture.checkResult("service<http> test {\n    \n}\n\nservice<>  {\n    \n}");
    //    }
    //
    //    public void testServiceBeforeService() {
    //        myFixture.configureByText("test.bal", "<caret>\nservice<http> test {\n    \n}\n");
    //        myFixture.type("ser\t");
    //        myFixture.checkResult("\nservice<>  {\n    \n}\nservice<http> test {\n    \n}\n");
    //    }
    //
    //    public void testStructInEmptyFile() {
    //        myFixture.configureByText("test.bal", "<caret>");
    //        myFixture.type("str\t");
    //        myFixture.checkResult("struct  {\n    \n}");
    //    }
    //
    //    public void testStructAfterService() {
    //        myFixture.configureByText("test.bal", "struct test {\n    \n}\n<caret>");
    //        myFixture.type("str\t");
    //        myFixture.checkResult("struct test {\n    \n}\nstruct  {\n    \n}");
    //    }
    //
    //    public void testStructBeforeStruct() {
    //        myFixture.configureByText("test.bal", "<caret>\nstruct test {\n    \n}\n");
    //        myFixture.type("str\t");
    //        myFixture.checkResult("struct  {\n    \n}\nstruct test {\n    \n}\n");
    //    }

    public void testWorkerInFunction() {
        myFixture.configureByText("test.bal", "function test () {\n    <caret>\n}");
        myFixture.type("wor\t");
        myFixture.checkResult("function test () {\n    worker  {\n        \n    }\n}");
    }

    public void testWorkerInResource() {
        myFixture.configureByText("test.bal", "service<http> test {\n    resource test (message m) {\n        " +
                "<caret>\n    }\n}");
        myFixture.type("wor\t");
        myFixture.checkResult("service<http> test {\n    resource test (message m) {\n        worker  {\n            " +
                "\n        }\n    }\n}");
    }

    //    public void testWorkerInAction() {
    //        myFixture.configureByText("test.bal", "connector test() {\n    action test () {\n        <caret>\n" +
    //                "    }\n}");
    //        myFixture.type("wor\t");
    //        myFixture.checkResult("connector test() {\n    action test () {\n        worker  {\n            \n    " +
    //                "    }\n    }\n}");
    //    }

    public void testWorkerAfterStatement() {
        myFixture.configureByText("test.bal", "function test () {\n    int a = 10;\n    <caret>\n}");
        myFixture.type("wor\t");
        myFixture.checkResult("function test () {\n    int a = 10;\n    worker  {\n        \n    }\n}");
    }

    public void testWorkerAfterConnectorVarDefStatement() {
        myFixture.configureByText("test.bal", "function test () {\n    TestConnector t = create TestConnector();\n " +
                "   <caret>\n}");
        myFixture.type("wor\t");
        myFixture.checkResult("function test () {\n    TestConnector t = create TestConnector();\n    worker  {\n    " +
                "    \n    }\n}");
    }

    public void testForkJoinInFunction() {
        myFixture.configureByText("test.bal", "function test () {\n    <caret>\n}");
        myFixture.type("fojo\t");
        myFixture.checkResult("function test () {\n    fork {\n        \n    } join () () {\n        \n    }\n}");
    }

    public void testForkJoinInResource() {
        myFixture.configureByText("test.bal", "service<http> test {\n    resource test (message m) {\n        " +
                "<caret>\n    }\n}");
        myFixture.type("fojo\t");
        myFixture.checkResult("service<http> test {\n    resource test (message m) {\n        " +
                "fork {\n            \n        } join () () {\n            \n        }\n    }\n}");
    }

    //    public void testForkJoinInAction() {
    //        myFixture.configureByText("test.bal", "connector test() {\n    action test () {\n        <caret>\n" +
    //                "    }\n}");
    //        myFixture.type("fojo\t");
    //        myFixture.checkResult("connector test() {\n    action test () {\n        " +
    //                "fork {\n            \n        } join () () {\n            \n        }\n    }\n}");
    //    }

    public void testForkJoinAfterStatement() {
        myFixture.configureByText("test.bal", "function test () {\n    int a = 10;\n    <caret>\n}");
        myFixture.type("fojo\t");
        myFixture.checkResult("function test () {\n    int a = 10;\n    fork {\n        \n    } join () () {\n       " +
                " \n    }\n}");
    }

    public void testForkJoinBeforeStatement() {
        myFixture.configureByText("test.bal", "function test () {\n    <caret>\n    int a = 10;\n}");
        myFixture.type("fojo\t");
        myFixture.checkResult("function test () {\n    fork {\n        \n    } join () () {\n        \n    }\n    " +
                "int a = 10;\n}");
    }

    //    public void testTransformer() {
    //        myFixture.configureByText("test.bal", "<caret>");
    //        myFixture.type("tra\t");
    //        myFixture.checkResult("transformer <, > () {\n    \n}");
    //    }
}
