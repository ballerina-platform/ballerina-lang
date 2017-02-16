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
import org.wso2.ballerina.core.model.BLangPackage;
import org.wso2.ballerina.docgen.docs.html.HtmlDocumentWriter;
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

        String userDir = System.getProperty("user.dir");
        String balPackagePath = userDir + File.separator + "src" + File.separator + "test" + File.separator
                + "resources" + File.separator + "balFiles" + File.separator + "htmlWriter";
        String outputPath =  userDir + File.separator + "api-docs" + File.separator + "html";
        String outputFilePath1 = outputPath + File.separator + "foo.bar.html";
        String outputFilePath2 = outputPath + File.separator + "foo.bar.xyz.html";
        String outputFilePath3 = outputPath + File.separator + "foo.bar.xyz.str.html";
        String indexOutputFilePath = outputPath + File.separator + "index.html";

        try {
            // Delete if file already exists
            deleteFile(outputFilePath1);
            deleteFile(outputFilePath2);
            deleteFile(outputFilePath3);
            deleteFile(indexOutputFilePath);

            // Generate HTML file
            Map<String, BLangPackage> packageMap =
                    BallerinaDocGeneratorMain.generatePackageDocsFromBallerina(balPackagePath);
            HtmlDocumentWriter htmlDocumentWriter = new HtmlDocumentWriter();
            htmlDocumentWriter.write(packageMap.values());

            // Assert file creation
            File htmlFile1 = new File(outputFilePath1);
            Assert.assertTrue(htmlFile1.exists());
            File htmlFile2 = new File(outputFilePath2);
            Assert.assertTrue(htmlFile2.exists());
            File htmlFile3 = new File(outputFilePath3);
            Assert.assertTrue(htmlFile3.exists());
            File indexHtmlFile = new File(indexOutputFilePath);
            Assert.assertTrue(indexHtmlFile.exists());

            // Assert function definitions
            String content1 = new Scanner(htmlFile1).useDelimiter("\\Z").next();
            Assert.assertTrue(content1.contains("function addHeader(<a href=\"#message\">message</a> m, "
                    + "<a href=\"#string\">string</a> key, <a href=\"#string\">string</a> value)"));
            Assert.assertTrue(content1.contains("function getHeader(<a href=\"#message\">message</a> m, "
                    + "<a href=\"#string\">string</a> key) (string value)"));
            Assert.assertTrue(content1.contains("Functions"));
            Assert.assertTrue(content1.contains("Connectors"));

            // asserting function @description
            Assert.assertTrue(content1.contains("<p>Get HTTP header from the message</p>"));
            // asserting function @param description
            Assert.assertTrue(content1.contains("<td>key</td><td><a href=\"#string\">string</a></td>"
                    + "<td>HTTP header key</td>"));
            // asserting function @return description
            Assert.assertTrue(content1.contains("<td>value</td><td><a href=\"#string\">string</a></td>"
                            + "<td>HTTP header value</td>"));
            
            // asserting connector @description
            Assert.assertTrue(content1.contains("<p>Test connector</p>"));
            // asserting connector @param description
            Assert.assertTrue(content1
                    .contains("<td>consumerKey</td><td><a href=\"#string\">string</a></td><td>consumer key</td>"));
            // asserting action @description
            Assert.assertTrue(content1.contains("<p>test connector action</p>"));
            // asserting action @param description
            Assert.assertTrue(content1.contains("<td>t</td><td><a href=\"foo.bar.html#TestConnector\" " +
                            "title=\"foo.bar:TestConnector\">TestConnector</a></td><td>a string argument</td>"));
            Assert.assertTrue(content1
                    .contains("<td>msg</td><td><a href=\"#string\">string</a></td><td>a string message</td>"));
            // asserting action @return description
            Assert.assertTrue(content1
                    .contains("<td>response</td><td><a href=\"#message\">message</a></td><td>response object</td>"));
            
            // Assert function and connector exclusion logic
            String content2 = new Scanner(htmlFile2).useDelimiter("\\Z").next();
            Assert.assertTrue(content2.contains("Functions"));
            Assert.assertFalse(content2.contains("Connectors"));
            Assert.assertTrue(content2.contains("Structs"));
            // asserting struct content
            Assert.assertTrue(content2.contains("<a href=\"#Argument \">"
                    + "Argument (string text, int argumentId, int sentiment)</a>"));

            String content3 = new Scanner(htmlFile3).useDelimiter("\\Z").next();
            Assert.assertFalse(content3.contains("Functions"));
            Assert.assertTrue(content3.contains("Connectors"));

            // Assert packages in index.html
            String content4 = new Scanner(indexHtmlFile).useDelimiter("\\Z").next();
            Assert.assertTrue(content4.contains("foo.bar"));
            Assert.assertTrue(content4.contains("foo.bar.xyz"));
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
            deleteFile(outputFilePath1);
            deleteFile(outputFilePath2);
            deleteFile(outputFilePath3);
            deleteFile(indexOutputFilePath);
        }
    }

    @Test(description = "HTML generation package exclusion test")
    public void testPackageExclusion() {

        String userDir = System.getProperty("user.dir");
        String balPackagePath = userDir + File.separator + "src" + File.separator + "test" + File.separator
                + "resources" + File.separator + "balFiles" + File.separator + "htmlWriter";
        String outputPath =  userDir + File.separator + "api-docs" + File.separator + "html";
        String outputFilePath1 = outputPath + File.separator + "foo.bar.html";
        String outputFilePath2 = outputPath + File.separator + "foo.bar.xyz.html";
        String indexOutputFilePath = outputPath + File.separator + "index.html";

        try {
            // Delete if file already exists
            deleteFile(outputFilePath1);
            deleteFile(outputFilePath2);

            // Generate HTML file
            Map<String, BLangPackage> packageMap =
                    BallerinaDocGeneratorMain.generatePackageDocsFromBallerina(balPackagePath, "foo.bar.xyz");
            HtmlDocumentWriter htmlDocumentWriter = new HtmlDocumentWriter();
            htmlDocumentWriter.write(packageMap.values());

            // Assert file exclusion
            File htmlFile1 = new File(outputFilePath1);
            Assert.assertTrue(htmlFile1.exists());
            File htmlFile2 = new File(outputFilePath2);
            Assert.assertFalse(htmlFile2.exists());
            File indexHtmlFile = new File(indexOutputFilePath);
            Assert.assertTrue(indexHtmlFile.exists());
        } catch (Exception e) {
            Assert.fail(e.getMessage());
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
            deleteFile(outputFilePath1);
            deleteFile(outputFilePath2);
            deleteFile(indexOutputFilePath);
        }
    }

    private void deleteFile(String filePath) {
        File htmlFile = new File(filePath);
        if (htmlFile.exists()) {
            out.println("Deleting file: " + htmlFile.getAbsolutePath());
            htmlFile.delete();
        }
    }
}
