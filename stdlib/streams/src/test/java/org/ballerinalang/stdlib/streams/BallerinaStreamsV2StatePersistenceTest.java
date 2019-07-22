/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.streams;

import org.apache.commons.io.FileUtils;
import org.ballerinalang.config.ConfigRegistry;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BValue;
import org.ballerinalang.tool.util.BCompileUtil;
import org.ballerinalang.tool.util.BRunUtil;
import org.ballerinalang.tool.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * This contains methods to test state persistence capability in
 * windows and aggregators of Ballerina Streaming V2.
 *
 * @since 0.990.5
 */
public class BallerinaStreamsV2StatePersistenceTest {
    private static final String PERSISTENCE_PATH = System.getProperty("ballerina.home") + File.separator +
            "persistence" + File.separator;
    private ConfigRegistry conf = ConfigRegistry.getInstance();
    private CompileResult result;

    @BeforeClass
    public void setup() {
        cleanSnapshots();
        result = BCompileUtil.compile("test-src/streamingv2-state-persistence-test.bal");
        conf.addConfiguration("b7a.streaming.persistence.directory", PERSISTENCE_PATH);
        conf.addConfiguration("b7a.streaming.persistence.enabled", true);
        conf.addConfiguration("b7a.streaming.persistence.interval", 1L);
    }

    @Test(description = "Test streaming state persistence", enabled = false)
    public void testStreamingStatePersistence() {
        BValue[] outputTeacherEvents = BRunUtil.invoke(result, "startQuery");
        Assert.assertNotNull(outputTeacherEvents);
        Assert.assertEquals(outputTeacherEvents.length, 2, "Expected events are not received");

        BMap<String, BValue> teacher0 = (BMap<String, BValue>) outputTeacherEvents[0];
        BMap<String, BValue> teacher1 = (BMap<String, BValue>) outputTeacherEvents[1];

        Assert.assertEquals(teacher0.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) teacher0.get("sumAge")).intValue(), 30);
        Assert.assertEquals(((BInteger) teacher0.get("count")).intValue(), 1);

        Assert.assertEquals(teacher1.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) teacher1.get("sumAge")).intValue(), 45);
        Assert.assertEquals(((BInteger) teacher1.get("count")).intValue(), 1);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException ignored) {
            // do nothing.
        }

        outputTeacherEvents = BRunUtil.invoke(result, "startQuery");
        Assert.assertNotNull(outputTeacherEvents);
        Assert.assertEquals(outputTeacherEvents.length, 2, "Expected events did not receive.");

        teacher0 = (BMap<String, BValue>) outputTeacherEvents[0];
        teacher1 = (BMap<String, BValue>) outputTeacherEvents[1];

        Assert.assertEquals(teacher0.get("name").stringValue(), "Mohan");
        Assert.assertEquals(((BInteger) teacher0.get("sumAge")).intValue(), 60);
        Assert.assertEquals(((BInteger) teacher0.get("count")).intValue(), 2);

        Assert.assertEquals(teacher1.get("name").stringValue(), "Raja");
        Assert.assertEquals(((BInteger) teacher1.get("sumAge")).intValue(), 90);
        Assert.assertEquals(((BInteger) teacher1.get("count")).intValue(), 2);
    }

    @AfterClass
    public void reset() {
        cleanSnapshots();
        conf.resetRegistry();
    }

    private void cleanSnapshots() {
        File snapshots = new File(PERSISTENCE_PATH);
        try {
            FileUtils.deleteDirectory(snapshots);
        } catch (IOException ignored) {
        }
    }
}
