/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.wso2.ballerina.core.parser.statements;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.BallerinaFunction;
import org.wso2.ballerina.core.utils.ParserUtils;

public class IfElseParserTest {

    @Test
    public void testFuncInvocation() {
        final String funcName = "test";
        BallerinaFile bFile = ParserUtils.parseBalFile("samples/statements/ifcondition.bal");
        BallerinaFunction function = (BallerinaFunction) bFile.getFunctions().get(funcName);
        Assert.assertNotNull(function);
    }

    // TODO: Add Negative test cases.

}
