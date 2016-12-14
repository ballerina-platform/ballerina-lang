/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.ballerina.core.parser;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.ballerina.core.model.Import;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

public class BallerinaBaseListenerImplTest {

    private BallerinaBaseListenerImpl ballerinaBaseListener;

    @BeforeTest
    public void setup() {
        try {
            ANTLRInputStream antlrInputStream = new ANTLRInputStream(new FileInputStream(
                    getClass().getClassLoader().getResource("samples/parser/HelloService.bal").getFile()));

            BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
            CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

            BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
            ballerinaBaseListener = new BallerinaBaseListenerImpl();

            ballerinaParser.addParseListener(ballerinaBaseListener);
            ballerinaParser.compilationUnit();

        } catch (IOException e) {
        }
    }

    @Test
    public void testPackageParser() {
        Assert.assertEquals(ballerinaBaseListener.balFile.getPackageName(), "samples.parser");
    }

    @Test
    public void testImportParser() {
        List<Import> imports = ballerinaBaseListener.balFile.getImports();
        Assert.assertEquals(imports.size(), 2);

        for (Import anImport : imports) {
            if (anImport.getPackageName().equalsIgnoreCase("ballerina.connectors.twitter")) {
                Assert.assertEquals(anImport.getImportName(), "twitter");
            } else if (anImport.getPackageName().equalsIgnoreCase("ballerina.connectors.salesforce")) {
                Assert.assertEquals(anImport.getImportName(), "sf");
            }
        }
    }

}
