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
 *
 */

package org.ballerinalang.test.nativeimpl.functions.io.protobuf;

import org.ballerinalang.launcher.util.BCompileUtil;
import org.ballerinalang.launcher.util.BRunUtil;
import org.ballerinalang.launcher.util.CompileResult;
import org.ballerinalang.model.values.BBoolean;
import org.ballerinalang.model.values.BString;
import org.ballerinalang.model.values.BValue;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.net.URISyntaxException;

public class ProtoChannelTest {
    private CompileResult protoChannelTest;
    private String currentDirectoryPath = "/tmp";

    @BeforeClass
    public void setup() {
        protoChannelTest = BCompileUtil.compileAndSetup("test-src/io/protobuf_io.bal");
        currentDirectoryPath = System.getProperty("user.dir") + "/target";
    }

    @Test(description = "Test 'test basic read/write of proto file'")
    public void testBasicProto() throws URISyntaxException {
        String sourceToWrite = currentDirectoryPath + "./proto.bin";
        //Will initialize the channel
        BValue[] args = {new BString(sourceToWrite)};
        BRunUtil.invokeStateful(protoChannelTest, "writeProtoDataToFile", args);

        BValue[] readProtoDataFiles = BRunUtil.invokeStateful(protoChannelTest, "readProtoDataFile", args);

        Assert.assertTrue(((BBoolean) readProtoDataFiles[0]).booleanValue());

    }
}
