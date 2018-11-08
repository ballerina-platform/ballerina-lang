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

package org.ballerinalang.testerina.test.coverage;

import org.ballerinalang.testerina.coverage.LCovSourceFile;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

public class LCovSourceFileTest {

    private LCovSourceFile lCovSourceFile;

    @BeforeTest
    public void beforeSuite() {

        lCovSourceFile = new LCovSourceFile("path", 1, 1);
    }

    @Test
    public void getSourceFilePathWithSetTest() {

        lCovSourceFile.setSourceFilePath("path1");
        Assert.assertEquals(lCovSourceFile.getSourceFilePath(), "path1");
    }

    @Test
    public void getNunLineExeWithSetTest() {

        lCovSourceFile.setNumOfLineExecuted(2);
        Assert.assertEquals(lCovSourceFile.getNumOfLineExecuted(), 2);
    }

    @Test
    public void getNumInstrumentedLinesWithSetTest() {

        lCovSourceFile.setNumOfInstrumentedLines(2);
        Assert.assertEquals(lCovSourceFile.getNumOfInstrumentedLines(), 2);
    }

    @Test
    public void getLCovFNListTest() {

        Assert.assertEquals(lCovSourceFile.getlCovFNList().size(), 0);
    }

    @Test
    public void getLCovFNDAListTest() {

        Assert.assertEquals(lCovSourceFile.getlCovFNDAList().size(), 0);
    }

    @Test
    public void getLCovFNFHBlockListTest() {

        Assert.assertEquals(lCovSourceFile.getlCovFNFHBlockList().size(), 0);
    }

    @Test
    public void getLCovBRDAListTest() {

        Assert.assertEquals(lCovSourceFile.getlCovBRDAList().size(), 0);
    }

    @Test
    public void getLCovBRFHBlockListTest() {

        Assert.assertEquals(lCovSourceFile.getlCovBRFHBlockList().size(), 0);
    }

    @Test
    public void getLCovDAListTest() {

        Assert.assertEquals(lCovSourceFile.getlCovDAList().size(), 0);
    }

}
