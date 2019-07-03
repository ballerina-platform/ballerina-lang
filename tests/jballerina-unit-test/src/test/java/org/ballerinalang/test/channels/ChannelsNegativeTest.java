/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.test.channels;

import org.ballerinalang.test.util.BCompileUtil;
import org.ballerinalang.test.util.CompileResult;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * Negative test for channels.
 *
 * @since 0.982.0
 */
public class ChannelsNegativeTest {

    private static final String CHANNEL_TEST = "ChannelsTest";
    private CompileResult result;

    @BeforeClass
    public void setup() {

        result = BCompileUtil.compile("test-src/channels/channel-negative-test.bal");
        Assert.assertEquals(result.getErrorCount(), 5, "Channels negative test error count");

    }

    @Test(description = "Test channel result's incompatible types", groups = CHANNEL_TEST)
    public void checkIncompatibleResultType() {
        Assert.assertEquals(result.getDiagnostics()[0].getPosition().getStartColumn(), 9, "Wrong channel position " +
                "column number");
        Assert.assertEquals(result.getDiagnostics()[0].getMessage(), "incompatible types: expected 'json', found " +
                "'string'", "Channel receive expression, incompatible result type error");

        Assert.assertEquals(result.getDiagnostics()[1].getPosition().getStartLine(), 12, "Wrong channel position " +
                "line number");
        Assert.assertEquals(result.getDiagnostics()[1].getMessage(), "incompatible types: expected 'json', found " +
                "'xml'", "Channel receive expression, incompatible result type error");

    }

    //todo:should change once we support other types as well
    @Test(description = "Test unsupported channel constraint types", groups = CHANNEL_TEST)
    public void checkChannelConstraintErrors() {

        Assert.assertEquals(result.getDiagnostics()[2].getPosition().getStartLine(), 16, "Incorrect position for " +
                "channel constraint error");
        Assert.assertEquals(result.getDiagnostics()[2].getMessage(), "incompatible types: 'channel' cannot be " +
                "constrained with 'map'", "Channel constraint type error message");
        Assert.assertEquals(result.getDiagnostics()[2].getPosition().getStartColumn(), 1, "Incorrect column position " +
                "for " +
                "channel constraint error");
        Assert.assertEquals(result.getDiagnostics()[3].getMessage(), "incompatible types: 'channel' cannot be " +
                "constrained with '(json,json)'", "Channel constraint type error message");
    }

    @Test(description = "Test invalid annotations", groups = CHANNEL_TEST)
    public void checkInvalidAnnotations() {
        Assert.assertEquals(result.getDiagnostics()[4].getPosition().getStartLine(), 27, "Wrong channel annotation " +
                "position line number");
        Assert.assertEquals(result.getDiagnostics()[4].getMessage(), "annotation 'ballerina/builtin:sensitive' is " +
                "not allowed on channel", "@sensitive annotation for channels error message");
    }
}
