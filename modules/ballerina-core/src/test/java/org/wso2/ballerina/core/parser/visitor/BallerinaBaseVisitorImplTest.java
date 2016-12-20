/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.parser.visitor;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.wso2.ballerina.core.parser.BallerinaLexer;
import org.wso2.ballerina.core.parser.BallerinaParser;

import java.io.FileInputStream;
import java.io.IOException;

public class BallerinaBaseVisitorImplTest {
    private CompilationUnitVisitor ballerinaBaseVisitor;

//    @BeforeTest
    public void setup() {
        try {
            ANTLRInputStream antlrInputStream = new ANTLRInputStream(new FileInputStream(
                    getClass().getClassLoader().getResource("samples/parser/VisitSample.bal").getFile()));
            BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
            CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);

            BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
            ballerinaBaseVisitor = new CompilationUnitVisitor();
            ballerinaBaseVisitor.visit(ballerinaParser.compilationUnit());

        } catch (IOException e) {
        }
    }

//    @Test
//    public void testPackageParser() {
//        Assert.assertEquals(ballerinaBaseVisitor.balFile.getPath(), "samples.parser");
//    }
//
//    @Test
//    public void testImportParser() {
//        List<Import> imports = ballerinaBaseVisitor.balFile.getImports();
//        Assert.assertEquals(imports.size(), 2);
//
//        for (Import anImport : imports) {
//            if (anImport.getPath().equalsIgnoreCase("ballerina.connectors.twitter")) {
//                Assert.assertEquals(anImport.getName(), "twitter");
//            } else if (anImport.getPath().equalsIgnoreCase("ballerina.connectors.salesforce")) {
//                Assert.assertEquals(anImport.getName(), "sf");
//            }
//        }
//    }

//    @Test
//    public void testServiceParser() {
//        List<Service> services = ballerinaBaseVisitor.balFile.getServices();
//        Assert.assertEquals(services.size(), 1);
//
//        for (Service aService : services) {
//                Assert.assertEquals(aService.getSymbolName().getName(), "HelloService");
//        }
//    }

}
