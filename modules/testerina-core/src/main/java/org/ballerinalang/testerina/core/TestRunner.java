/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.testerina.core;

//import org.ballerinalang.testerina.core.langutils.Functions;
import org.ballerinalang.testerina.core.entity.TesterinaFile;
import org.ballerinalang.testerina.core.entity.TesterinaFunction;
import org.ballerinalang.testerina.core.langutils.FunctionUtils;
import org.ballerinalang.testerina.core.langutils.ParserUtils;
import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.SymScope;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintlnInt;
import org.wso2.ballerina.core.nativeimpl.lang.system.PrintlnString;
//import org.wso2.ballerina.core.model.BallerinaFile;
//import org.wso2.ballerina.core.model.values.BValue;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * TestRunner entity class
 */
public class TestRunner {

    private BallerinaFile bFile;

    java.util.logging.Logger logger = java.util.logging.Logger.getLogger("TestRunner");

    public void runMain(Path sourceFilePath) {

        logger.info("sourceFilePath: " + sourceFilePath.toString());
        SymScope symScope = new SymScope(null);
        FunctionUtils.addNativeFunction(symScope, new PrintlnInt());
        FunctionUtils.addNativeFunction(symScope, new PrintlnString());
        bFile = ParserUtils.parseBalFile(sourceFilePath.toString(), symScope, true);

        TesterinaFile tFile = new TesterinaFile(getFileNameFromResourcePath(sourceFilePath.toString()),
                sourceFilePath.toString(), bFile);
        ArrayList<TesterinaFunction> testFunctions = tFile.getTestFunctions();
        Iterator<TesterinaFunction> testFuncIter = testFunctions.iterator();
        while (testFuncIter.hasNext()) {
            TesterinaFunction tFunc = testFuncIter.next();
            logger.info("Running '" + tFunc.getName() + "'...");
            try {
                tFunc.invoke();
            } catch (BallerinaException e) {
                logger.info(e.getMessage());
            }
        }

        //BValue[] returns = Functions.invoke(bFile, "testFunction1");
        //log.info("return: " + returns[0]);
    }

    private String getFileNameFromResourcePath(String path) {
        int indexOfLastSeparator = path.lastIndexOf(File.separator);
        String fileName = path.substring(indexOfLastSeparator + 1);
        return fileName;
    }
}
