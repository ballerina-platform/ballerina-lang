/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.query.test;

import org.junit.Assert;
import org.junit.Test;
import org.wso2.siddhi.query.api.execution.Subscription;
import org.wso2.siddhi.query.compiler.SiddhiCompiler;
import org.wso2.siddhi.query.compiler.exception.SiddhiParserException;

public class DefineSubscriptionTestCase {

    @Test
    public void testCreatingHttpSubscriptionXmlMapping() throws SiddhiParserException{
        Subscription subscription = SiddhiCompiler.parseSubscription("subscribe http options(context '/test', transport 'http,https') " +
                                                        "map xml " +
                                                        "options( xmlns \"http://www.w3.org/TR/html4/\") " +
                                                        "\"//h:time\", \"//h:data\" " +
                                                        "Insert into FooStream;");
        Assert.assertNotNull(subscription);
    }

    @Test
    public void testCreatingHttpSubscriptionJsonMapping() throws SiddhiParserException{
        Subscription subscription = SiddhiCompiler.parseSubscription("subscribe jms options(topic 'foo') " +
                "map json \"$.sensorData.time\", \"$.sensorData.data\" " +
                "Insert into FooStream;");
        Assert.assertNotNull(subscription);
    }

    @Test
    public void testCreatingJmsSubscriptionJsonMapping() throws SiddhiParserException{
        Subscription subscription = SiddhiCompiler.parseSubscription("subscribe jms options(topic 'foo') " +
                                                            "map json " +
                                                            "Insert into FooStream;");
        Assert.assertNotNull(subscription);
    }

    @Test
    public void testCreatingJmsSubscriptionTextMapping() throws SiddhiParserException{
        Subscription subscription =SiddhiCompiler.parseSubscription("subscribe jms options(topic 'foo') " +
                "map text " +
                "options ( regex1  \"(\\w+)\\s(\\w+)\\s(\\w+)\\s(\\w+)\", " +
                "regex2  \"(\\w+)\\s(\\w+)\\s(\\w+)\\s(\\w+)\") " +
                "\"regex1[1]\" , \"regex2[3]\" " +
                "insert into FooStream;");
        Assert.assertNotNull(subscription);
    }
}
