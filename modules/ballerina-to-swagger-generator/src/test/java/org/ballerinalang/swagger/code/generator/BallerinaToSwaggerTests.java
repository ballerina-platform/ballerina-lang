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

package org.ballerinalang.swagger.code.generator;

//import com.google.gson.JsonParser;
//import org.ballerinalang.swagger.code.generator.exception.SwaggerGenException;
//import org.ballerinalang.util.codegen.PackageInfo;
//import org.ballerinalang.util.codegen.ProgramFile;
//import org.testng.Assert;
//import org.testng.annotations.DataProvider;
//import org.testng.annotations.Test;
//import org.ballerinalang.swagger.code.generator.utils.BTestUtils;
//
//import java.io.File;
//import java.io.IOException;
//import java.net.URL;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//import java.util.ArrayList;
//import java.util.List;

/**
 * Test class for testing swagger generation using ballerina source.
 */
public class BallerinaToSwaggerTests {

    //TODO fix this properly
//    private JsonParser parser = new JsonParser();
//
//    /**
//     * Data provider for swagger test cases.
//     *
//     * @return List of swagger test cases.
//     */
//    @DataProvider(name = "SwaggerSamples")
//    public Object[][] swaggerSamples() throws IOException {
//        ClassLoader classLoader = getClass().getClassLoader();
//        URL inputFolderUrl = classLoader.getResource("input" + File.separator);
//        List<String> fileNames = new ArrayList<>();
//        if (null != inputFolderUrl) {
//            File inputFolder = new File(inputFolderUrl.getFile());
//            if (null != inputFolder.listFiles()) {
//                for (File balFile : inputFolder.listFiles()) {
//                    fileNames.add(this.removeExtension(balFile.getName()));
//                }
//            }
//        }
//
//        return fileNames.stream().map(fileName -> {
//            URL inputBalUrl = classLoader.getResource("input" + File.separator + fileName + ".bal");
//            URL outputSwaggerUrl = classLoader.getResource("output" + File.separator + fileName + ".json");
//            if (null != inputBalUrl && null != outputSwaggerUrl) {
//                return new Object[]{inputBalUrl.getFile(), outputSwaggerUrl.getFile()};
//            } else {
//                return new Object[]{null, null};
//            }
//        }).filter(i -> null != i[0])
//                .toArray(Object[][]::new);
//    }
//
//    /**
//     * Execute tests converting ballerina source to swagger definitions.
//     * @param inputFilePath The input bal file.
//     * @param outputFilePath The expected swagger definition file.
//     * @throws IOException When files cannot be read.
//     * @throws SwaggerGenException When an exception occurs during swagger definition gen.
//     */
//    @Test(dataProvider = "SwaggerSamples")
//    public void testBallerinaToSwaggerConversionWithServiceInfo(String inputFilePath, String outputFilePath)
//            throws IOException, SwaggerGenException {
//        ProgramFile programFile = BTestUtils.getProgramFile(inputFilePath);
//        PackageInfo packageInfo = programFile.getPackageInfo(".");
//        String generatedSwagger = BallerinaToSwaggerGenerator.generateSwagger(packageInfo.getServiceInfoEntries()[0]);
//
//        String expectedSwagger = new String(Files.readAllBytes(Paths.get(outputFilePath)));
//        Assert.assertTrue(parser.parse(generatedSwagger).equals(parser.parse(expectedSwagger)),
//                "Invalid Swagger definition generated.\nExpected: " + parser.parse(expectedSwagger).toString() +
//                "\nActual: " + parser.parse(generatedSwagger).toString());
//    }
//
//    /**
//     * Removes the extension of a file name.
//     * @param filename The name of the file.
//     * @return The file name without the extension.
//     */
//    private String removeExtension(String filename) {
//        int extensionPos = filename.lastIndexOf(".");
//        int lastUnixPos = filename.lastIndexOf("/");
//        int lastWindowsPos = filename.lastIndexOf("\\");
//        int lastSeparator = Math.max(lastUnixPos, lastWindowsPos);
//        int index = lastSeparator > extensionPos ? -1 : extensionPos;
//
//        if (-1 == index) {
//            return filename;
//        } else {
//            return filename.substring(0, index);
//        }
//    }
}
