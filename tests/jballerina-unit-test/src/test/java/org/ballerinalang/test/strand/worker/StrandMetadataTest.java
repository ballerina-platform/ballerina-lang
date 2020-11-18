/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.test.strand.worker;

import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.BRunUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Class to test functionality of strand meta data.
 *
 * @since 2.0.0
 */
public class StrandMetadataTest {

    private CompileResult compileResult;

    @BeforeClass
    public void setup() {
        this.compileResult = BCompileUtil.compile("test-src/strand/strand-meta-data.bal");
    }

    @Test
    public void testStrandMetadataAsyncCalls() {
        BRunUtil.invoke(compileResult, "testStrandMetadataAsyncCalls", new BValue[0]);
    }
}
