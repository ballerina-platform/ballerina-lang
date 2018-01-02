/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.test.mime;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BStruct;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.net.http.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import static org.ballerinalang.net.mime.util.Constants.FILE;
import static org.ballerinalang.net.mime.util.Constants.MEDIA_TYPE;
import static org.ballerinalang.net.mime.util.Constants.OVERFLOW_DATA_INDEX;
import static org.ballerinalang.net.mime.util.Constants.PROTOCOL_PACKAGE_FILE;
import static org.ballerinalang.net.mime.util.Constants.PROTOCOL_PACKAGE_MIME;

/**
 * Unit tests for MIME package utilities.
 */
public class MimeTest {
    private static final Logger LOG = LoggerFactory.getLogger(MimeTest.class);

    private CompileResult compileResult;
    private final String protocolPackageMime = PROTOCOL_PACKAGE_MIME;
    private final String protocolPackageFile = PROTOCOL_PACKAGE_FILE;
    private final String entityStruct = Constants.ENTITY;
    private final String mediaTypeStruct = MEDIA_TYPE;

    @BeforeClass
    public void setup() {
        compileResult = BCompileUtil.compile("test-src/mime/mime-utility-functions.bal");
    }

    @Test(description = "Test 'getText' function in ballerina.net.mime package")
    public void testGetTextInFile() {
        BStruct entity = BCompileUtil
                .createAndGetStruct(compileResult.getProgFile(), protocolPackageMime, entityStruct);
        try {
            String payload = "ballerina";
            File file = File.createTempFile("test", ".txt");
            file.deleteOnExit();
            BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write(payload);
            bufferedWriter.close();

            BStruct fileStruct = BCompileUtil
                    .createAndGetStruct(compileResult.getProgFile(), protocolPackageFile, FILE);
            fileStruct.setStringField(0, file.getAbsolutePath());
            entity.setRefField(OVERFLOW_DATA_INDEX, fileStruct);
            BValue[] args = { entity };
            BValue[] returns = BRunUtil.invoke(compileResult, "testGetTextInFile", args);

            Assert.assertEquals(returns.length, 1);
            Assert.assertEquals(returns[0].stringValue(), payload);
        } catch (IOException e) {
            LOG.error("Error occured in testGetTextInFile", e.getMessage());
        }

    }
}
