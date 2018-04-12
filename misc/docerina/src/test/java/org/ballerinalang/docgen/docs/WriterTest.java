/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.docgen.docs;

import org.ballerinalang.docgen.Writer;
import org.ballerinalang.docgen.model.Page;
import org.ballerinalang.docgen.model.StaticCaption;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;
import java.util.ArrayList;

/**
 * Test cases for Handlebars Writer.
 */
@Test(groups = "broken")
public class WriterTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private PrintStream defaultOutput;
    @BeforeClass
    public void setup() {
        defaultOutput = System.out;
        System.setOut(new PrintStream(outContent));
    }
    
    @Test(description = "Test if writer class is working.", enabled = false)
    public void testWriter() {
        URL location = Writer.class.getProtectionDomain().getCodeSource().getLocation();
        String outputPath = location.getPath() + File.separator + "sample.html";
        Writer.writeHtmlDocument(
                new Page(new StaticCaption(""), new ArrayList<>(), new ArrayList<>()),
                "page", outputPath);
    }
    
    @Test(description = "Test writer with invalid output path.", enabled = false)
    public void testWriterWithInvalidPath() {
        URL location = Writer.class.getProtectionDomain().getCodeSource().getLocation();
        String outputPath = location.getPath();
        Writer.writeHtmlDocument(
                new Page(new StaticCaption(""), new ArrayList<>(), new ArrayList<>()),
                "page", outputPath);
        Assert.assertTrue(outContent.toString().contains("docerina: could not write HTML file"),
                "An error was expected");
    }
    
    @AfterClass
    public void cleanup() {
        System.setOut(defaultOutput);
    }
}
