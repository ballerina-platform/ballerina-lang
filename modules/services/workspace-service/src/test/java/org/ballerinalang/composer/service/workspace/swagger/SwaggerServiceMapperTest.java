/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ballerinalang.composer.service.workspace.swagger;

import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Test classes for swagger services.
 */
public class SwaggerServiceMapperTest {
    private JsonParser parser = new JsonParser();
    
    /**
     * Data provider for swagger test cases.
     *
     * @return List of swagger test cases.
     */
    @DataProvider(name = "SwaggerSamples")
    public Object[][] swaggerSamples() throws IOException {
        String testFilesPath = "samples" + File.separator + "swagger" + File.separator + "ballerina-to-swagger" +
                               File.separator;
       ClassLoader classLoader = getClass().getClassLoader();
        URL inputFolderUrl = classLoader.getResource(testFilesPath + "input" + File.separator);
        List<String> fileNames = new ArrayList<>();
        if (null != inputFolderUrl) {
            File inputFolder = new File(inputFolderUrl.getFile());
            if (null != inputFolder.listFiles()) {
                for (File balFile : inputFolder.listFiles()) {
                    fileNames.add(this.removeExtension(balFile.getName()));
                }
            }
        }
        
        return fileNames.stream().map(fileName -> {
            try {
                URL inputBalUrl = classLoader.getResource(testFilesPath + "input" + File.separator + fileName + ".bal");
                URL outputSwaggerUrl = classLoader.getResource(testFilesPath + "output" + File.separator + fileName +
                                                               ".json");
                if (null != inputBalUrl && null != outputSwaggerUrl) {
                        String inputFileContent =
                                FileUtils.readFileToString(new File(inputBalUrl.getFile()), Charset.defaultCharset());
                        String outputFileContent =
                            FileUtils.readFileToString(new File(outputSwaggerUrl.getFile()), Charset.defaultCharset());
                    return new Object[]{inputFileContent, outputFileContent};
                } else {
                    return new Object[]{null, null};
                }
            } catch (IOException e) {
                return new Object[]{null, null};
            }
        }).filter(i -> null != i[0])
                .toArray(Object[][]::new);
    }
    
    /**
     * Execute tests converting ballerina source to swagger definitions.
     * @param ballerinaSource The ballerina source.
     * @param expectedSwagger The swagger json.
     * @throws IOException When sample files cannot be read.
     */
    @Test(dataProvider = "SwaggerSamples")
    public void testBallerinaToSwaggerConversion(String ballerinaSource, String expectedSwagger) throws IOException {
        String generatedSwagger = SwaggerConverterUtils.generateSwaggerDefinitions(ballerinaSource, null);
    
        Assert.assertTrue(parser.parse(generatedSwagger).equals(parser.parse(expectedSwagger)),
                "Invalid Swagger definition generated.\nExpected: " + parser.parse(expectedSwagger).toString() +
                "\nActual: " + parser.parse(generatedSwagger).toString());
    }
    
    /**
     * Removes the extension of a file name.
     * @param filename The name of the file.
     * @return The file name without the extension.
     */
    private String removeExtension(String filename) {
        int extensionPos = filename.lastIndexOf(".");
        int lastUnixPos = filename.lastIndexOf("/");
        int lastWindowsPos = filename.lastIndexOf("\\");
        int lastSeparator = Math.max(lastUnixPos, lastWindowsPos);
        int index = lastSeparator > extensionPos ? -1 : extensionPos;
        
        if (-1 == index) {
            return filename;
        } else {
            return filename.substring(0, index);
        }
    }
}
