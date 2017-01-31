/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.testerina.core;

import org.wso2.ballerina.core.exception.BallerinaException;

import java.io.File;
import java.io.FilenameFilter;

public class TesterinaRunner {

    static final String suffix = "_test.bal";

     public static void main(String[] args) {
         String resource_path = "/home/nirodha/wso2/bal/testFiles/";
         File resourceFile = new File(resource_path);
         TesterinaExecuter testExec = new TesterinaExecuter();

         if(resourceFile.isDirectory()){
             // if the resource path is a directory execute all the test files
             FilenameFilter filter = new FilenameFilter() {
                 @Override
                 public boolean accept(File dir, String name) {
                     return name.endsWith(suffix);
                     //return true;
                 }
             };
             File[] files = resourceFile.listFiles(filter);
             for(int i=0; i < files.length; i++){
                 String path = files[i].getPath();
                 System.out.println("Executing test file : " + path);
                 testExec.executeTestFile(path);
             }
         }

         if(resourceFile.isFile()) {
             if (resourceFile.getName().endsWith(suffix)) {
                 testExec.executeTestFile(resource_path);
             } else {
                 throw new BallerinaException("Error: Invalid File Naming..");
             }
         }
     }
}
