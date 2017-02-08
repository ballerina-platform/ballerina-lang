/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*  http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerina.lang.values;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.runtime.internal.BuiltInNativeConstructLoader;
import org.wso2.ballerina.core.utils.ParserUtils;
import org.wso2.ballerina.lang.util.Functions;

import java.io.IOException;

/**
 * Test class for string escape characters test.
 * system:println("hello\n");
 */
public class EscapeCharacterTest {
    private BallerinaFile bFile;

    @BeforeClass(alwaysRun = true)
    public void setup() {
//        FunctionUtils.addNativeFunction(symScope, new PrintString());
        BuiltInNativeConstructLoader.loadConstructs();
        bFile = ParserUtils.parseBalFile("lang/values/string-newline.bal");
    }

    @Test(description = "Test new line character in string")
    public void testNewlineCharacter() {
        java.io.ByteArrayOutputStream out = null;
        java.io.PrintStream mainStream = System.out;
        try {
            out = new java.io.ByteArrayOutputStream();
            System.setOut(new java.io.PrintStream(out));
            Functions.invoke(bFile, "printNewline");
            String outPut = out.toString();
            Assert.assertNotNull(outPut, "string is not printed");
            //getting the last new line character
            Assert.assertEquals(outPut.charAt(outPut.length() - 1), '\n'
                    , "New line character not found in output string");
        } finally {
            System.setOut(mainStream);
            try {
                out.close();
            } catch (IOException e) {
                //ignore
            }
        }

    }
}
