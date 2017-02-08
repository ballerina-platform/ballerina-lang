///*
// * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
// *
// * WSO2 Inc. licenses this file to you under the Apache License,
// * Version 2.0 (the "License"); you may not use this file except
// * in compliance with the License.
// * You may obtain a copy of the License at
// *
// *    http://www.apache.org/licenses/LICENSE-2.0
// *
// * Unless required by applicable law or agreed to in writing,
// * software distributed under the License is distributed on an
// * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// * KIND, either express or implied.  See the License for the
// * specific language governing permissions and limitations
// * under the License.
// */
//package org.ballerinalang.testerina.core;
//
//import org.wso2.ballerina.core.exception.BallerinaException;
//
//import java.io.File;
//import java.io.FilenameFilter;
//import java.nio.file.Path;
//import java.util.logging.Logger;
//
///**
// * TesterinaRunner class
// */
//public class TesterinaRunner {
//
//    static final String SUFFIX = "_test.bal";
//
//    static void runMain(Path sourceFilePath) {
//         String resourcePath = sourceFilePath.toString();
//         File resourceFile = new File(resourcePath);
//         TesterinaExecuter testExec = new TesterinaExecuter();
//         Logger logger = Logger.getLogger("TesterinaRunner");
//
//         if (resourceFile.isDirectory()) {
//             // if the resource path is a directory execute all the test files
//             FilenameFilter filter = new FilenameFilter() {
//                 @Override
//                 public boolean accept(File dir, String name) {
//                     return name.endsWith(SUFFIX);
//                     //return true;
//                 }
//             };
//             File[] files = resourceFile.listFiles(filter);
//             for (File file: files) {
//                 String path = file.getPath();
//                 logger.info("Executing test file : " + path);
//                 testExec.executeTestFile(path);
//             }
//         }
//
//         if (resourceFile.isFile()) {
//             if (resourceFile.getName().endsWith(SUFFIX)) {
//                 testExec.executeTestFile(resourcePath);
//             } else {
//                 throw new BallerinaException("Error: Invalid File Naming..");
//             }
//         }
//     }
//}
