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
package org.ballerinalang.docgen.docs;

import org.ballerinalang.docgen.docs.html.HtmlDocumentWriter;
import org.ballerinalang.docgen.docs.utils.BallerinaDocGenTestUtils;
import org.ballerinalang.model.BLangPackage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Scanner;

/**
 * HTML document writer test.
 */
public class HtmlDocumentWriterTest {

    private static PrintStream out = System.out;

    @Test(description = "HTML generation test")
    public void testHtmlGeneration() throws Exception {

        String userDir = System.getProperty("user.dir");
        String balPackagePath = userDir + File.separator + "src" + File.separator + "test" + File.separator
                + "resources" + File.separator + "balFiles" + File.separator + "htmlWriter";
        String outputPath =  userDir + File.separator + "target" + File.separator +
                "api-docs1";
        String outputFilePath1 = outputPath + File.separator + "foo.bar.html";
        String outputFilePath2 = outputPath + File.separator + "foo.bar.xyz.html";
        String outputFilePath3 = outputPath + File.separator + "foo.bar.xyz.str.html";
        String indexOutputFilePath = outputPath + File.separator + "index.html";

        createOutputDirectory(outputPath);
        System.setProperty("html.output.path", outputPath);

        try {
            // Generate HTML file
            Map<String, BLangPackage> packageMap =
                    BallerinaDocGenerator.generatePackageDocsFromBallerina(balPackagePath);
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
                    + "<a href=\"#string\">string</a> key) (string )"));
            Assert.assertTrue(content1.contains("Functions"));
            Assert.assertTrue(content1.contains("Connectors"));

            // asserting function @description
            Assert.assertTrue(content1.contains("<p>Get HTTP header from the message</p>"));
            // asserting function @param description
            Assert.assertTrue(content1.contains("<td>key</td><td><a href=\"#string\">string</a></td>"
                    + "<td>HTTP header key</td>"));
            // asserting function @return description
            Assert.assertTrue(content1.contains("<td></td><td><a href=\"#string\">string</a></td>"
                            + "<td>HTTP header value</td>"));
            
            // asserting connector @description
            Assert.assertTrue(content1.contains("<p>Test connector</p>"));
            // asserting connector @param description
            Assert.assertTrue(content1
                    .contains("<td>consumerKey</td><td><a href=\"#string\">string</a></td><td>consumer key</td>"));
            // asserting action @description
            Assert.assertTrue(content1.contains("<p>test connector action</p>"));
            // asserting action @param description
            Assert.assertTrue(content1
                    .contains("<td>msg</td><td><a href=\"#string\">string</a></td><td>a string message</td>"));
            // asserting action @return description
            Assert.assertTrue(content1
                    .contains("<td>response</td><td><a href=\"#message\">message</a></td><td>response object</td>"));
            // asserting struct content
            Assert.assertTrue(content1.contains("struct Argument"));
            Assert.assertTrue(content1
                    .contains("<td>text</td><td><a href=\"#string\">string</a></td><td>a string</td>"));
            
            // Assert function and connector exclusion logic
            String content2 = new Scanner(htmlFile2).useDelimiter("\\Z").next();
            Assert.assertTrue(content2.contains("Functions"));
            Assert.assertFalse(content2.contains("Connectors"));
            Assert.assertTrue(content2.contains("Structs"));

            String content3 = new Scanner(htmlFile3).useDelimiter("\\Z").next();
            Assert.assertFalse(content3.contains("Functions"));
            Assert.assertTrue(content3.contains("Connectors"));

            // Assert packages in index.html
            String content4 = new Scanner(indexHtmlFile).useDelimiter("\\Z").next();
            Assert.assertTrue(content4.contains("foo.bar"));
            Assert.assertTrue(content4.contains("foo.bar.xyz"));
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
            BallerinaDocGenTestUtils.deleteDirectory(outputPath);
        }
    }

    @Test(description = "HTML generation package exclusion test")
    public void testPackageExclusion() throws Exception {

        String userDir = System.getProperty("user.dir");
        String balPackagePath = userDir + File.separator + "src" + File.separator + "test" + File.separator
                + "resources" + File.separator + "balFiles" + File.separator + "htmlWriter";
        String outputPath =  userDir + File.separator + "target" + File.separator +
                "api-docs2";
        String outputFilePath1 = outputPath + File.separator + "foo.bar.html";
        String outputFilePath2 = outputPath + File.separator + "foo.bar.xyz.html";
        String indexOutputFilePath = outputPath + File.separator + "index.html";

        try {
            createOutputDirectory(outputPath);
            System.setProperty("html.output.path", outputPath);

            // Generate HTML file
            Map<String, BLangPackage> packageMap =
                    BallerinaDocGenerator.generatePackageDocsFromBallerina(balPackagePath, "foo.bar.xyz");
            HtmlDocumentWriter htmlDocumentWriter = new HtmlDocumentWriter();
            htmlDocumentWriter.write(packageMap.values());

            // Assert file exclusion
            File htmlFile1 = new File(outputFilePath1);
            Assert.assertTrue(htmlFile1.exists());
            File htmlFile2 = new File(outputFilePath2);
            Assert.assertFalse(htmlFile2.exists());
            File indexHtmlFile = new File(indexOutputFilePath);
            Assert.assertTrue(indexHtmlFile.exists());
        } finally {
            BallerinaDocGenTestUtils.cleanUp();
            BallerinaDocGenTestUtils.deleteDirectory(outputPath);
        }
    }

    private void createOutputDirectory(String outputPath) throws IOException {
        // Delete output path if already exists
        BallerinaDocGenTestUtils.deleteDirectory(outputPath);
        Files.createDirectories(Paths.get(outputPath));
    }
}
