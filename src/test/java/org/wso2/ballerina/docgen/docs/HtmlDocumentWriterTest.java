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
import java.util.Scanner;

/**
 * HTML document writer test.
 */
public class HtmlDocumentWriterTest {

    private static PrintStream out = System.out;

    @Test(description = "HTML generation test")
    public void testHtmlGeneration() {
        try {
            String userDir = System.getProperty("user.dir");
            String balPackagePath = userDir + File.separator + "src" + File.separator + "test" + File.separator
                    + "resources" + File.separator + "balFiles" + File.separator + "htmlWriter";
            String outputPath =  userDir + File.separator + "api-docs" + File.separator + "html";
            String outputFilePath1 = outputPath + File.separator + "foo.bar.html";
            String outputFilePath2 = outputPath + File.separator + "foo.bar.xyz.html";

            // Delete if file already exists
            deleteFile(outputFilePath1);
            deleteFile(outputFilePath2);

            // Generate HTML file
            Map<String, BallerinaPackageDoc> docsMap =
                    BallerinaDocGeneratorMain.generatePackageDocsFromBallerina(balPackagePath);
            HtmlDocumentWriter htmlDocumentWriter = new HtmlDocumentWriter();
            htmlDocumentWriter.write(docsMap.values());

            // Assert file creation
            File htmlFile1 = new File(outputFilePath1);
            Assert.assertTrue(htmlFile1.exists());
            File htmlFile2 = new File(outputFilePath2);
            Assert.assertTrue(htmlFile2.exists());

            // Assert function definitions
            String content = new Scanner(htmlFile1).useDelimiter("\\Z").next();
            Assert.assertTrue(content.contains("function addHeader(message m, string key, string value)"));
            Assert.assertTrue(content.contains("function getHeader(message m, string key) (string value)"));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
        }
    }

    private void deleteFile(String outputFilePath1) {
        File htmlFile = new File(outputFilePath1);
        if (htmlFile.exists()) {
            out.println("Deleting existing file: " + htmlFile.getAbsolutePath());
            htmlFile.delete();
        }
    }
}
