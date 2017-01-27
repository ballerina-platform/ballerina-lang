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
package org.wso2.ballerina.docgen.docs;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.wso2.ballerina.docgen.docs.html.HtmlDocumentWriter;
import org.wso2.ballerina.docgen.docs.model.BallerinaPackageDoc;
import org.wso2.ballerina.docgen.docs.utils.BallerinaDocGenTestUtils;

import java.io.File;
import java.io.PrintStream;
import java.util.Map;

/**
 * HTML document writer test.
 */
public class HtmlDocumentWriterTest {

    private static PrintStream out = System.out;

    @Test(description = "HTML generation test")
    public void testHtmlGeneration() {
        try {
            String userDir = System.getProperty("user.dir");
            String balPath = userDir + File.separator + "src" + File.separator + "test" + File.separator + "resources"
                    + File.separator + "balFiles" + File.separator + "htmlWriter" + File.separator + "foo"
                    + File.separator + "bar";
            String outputPath =  userDir + File.separator + "api-docs" + File.separator + "html";
            String outputFilePath = outputPath + File.separator + "foo.bar.mediation.html";

            // Delete if file already exists
            File htmlFile = new File(outputFilePath);
            if (htmlFile.exists()) {
                out.println("Deleting existing file: " + htmlFile.getAbsolutePath());
                htmlFile.delete();
            }

            // Generate HTML file
            Map<String, BallerinaPackageDoc> docsMap =
                    BallerinaDocGeneratorMain.generatePackageDocsFromBallerina(balPath);
            HtmlDocumentWriter htmlDocumentWriter =
                    new HtmlDocumentWriter("templates" + File.separator + "html"
                            + File.separator + "package.vm", outputPath);
            htmlDocumentWriter.write(docsMap.values());
            htmlFile = new File(outputFilePath);

            // Assert file creation
            Assert.assertTrue(htmlFile.exists());
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }
}
