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

package org.ballerinalang.test.service.file.sample;

import org.ballerinalang.test.context.Constant;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;

/**
 * Testing the Echo service sample located in
 * ballerina_home/samples/fileService/orderProcessService.bal.
 */
public class OrderProcessingServiceSampleTestCase {

//    @Test(description = "Test whether files have been deleted after reading the content")
    public void testFileDeletion() throws IOException, InterruptedException {
        // Wait till relevant contents are read and files are deleted.
        Thread.sleep(5000);
        ClassLoader classLoader = getClass().getClassLoader();
        File folder = new File(classLoader.getResource(Constant.VFS_LOCATION + File.separator + "orders").getFile());
        File[] listOfFiles = folder.listFiles();
        Assert.assertEquals(0, listOfFiles.length, "Files are not deleted after content is read from the files");
    }
}
