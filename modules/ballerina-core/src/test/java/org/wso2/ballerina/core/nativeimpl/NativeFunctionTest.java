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
package org.wso2.ballerina.core.nativeimpl;

import org.testng.annotations.BeforeTest;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.nativeimpl.lang.system.Println;
import org.wso2.ballerina.core.utils.TestUtils;

public class NativeFunctionTest {

    private BallerinaFile bFile;
    private SymScope globalScope;

    @BeforeTest
    public void setup() {
        bFile = TestUtils.parseBalFile("samples/parser/ifcondition.bal");

        globalScope = new SymScope();

        Println printlnFunc = new Println();

        printlnFunc.getParameters();


    }



    public void testNativeFuncInvocation() {

    }

}
