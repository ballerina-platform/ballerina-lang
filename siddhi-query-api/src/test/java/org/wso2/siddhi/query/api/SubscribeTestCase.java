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
package org.wso2.siddhi.query.api;

import org.junit.Test;
import org.wso2.siddhi.query.api.execution.Subscription;
import org.wso2.siddhi.query.api.execution.io.map.Mapping;
import org.wso2.siddhi.query.api.execution.io.Transport;

public class SubscribeTestCase {

//    subscribe http options(context ‘/test’, transport ‘http,https’)
//    map xml “//h:time”, “//h:data”
//    options( xmlns:h “http://www.w3.org/TR/html4/”)
//            Insert into FooStream;
    @Test
    public void testCreatingHttpSubscriptionXmlMapping() {
        Subscription subscription = Subscription.Subscribe(
                Transport.transport("http").
                        option("context", "/test").
                        option("transport", "http,https"));

        subscription.map(Mapping.format("xml").
                map("/h:time").
                map("data", "//h:data").
                option("xmlns:h", "http://www.w3.org/TR/html4/"));

        subscription.insertInto("FooStream");

        ExecutionPlan.executionPlan("test").addSubscription(subscription);

    }

//    subscribe jms options(topic  ‘foo’)
//    map json "$.sensorData.time", "$.sensorData.data"
//    Insert into FooStream;
    @Test
    public void testCreatingHttpSubscriptionJsonMapping() {
        Subscription subscription = Subscription.Subscribe(
                Transport.transport("jms").
                        option("topic", "foo"));

        subscription.map(
                Mapping.format("json").
                        map("$.sensorData.time").
                        map("$.sensorData.data"));

        subscription.insertInto("FooStream");

        ExecutionPlan.executionPlan("test").addSubscription(subscription);

    }

//    --using default mapping
//    subscribe jms options(topic ‘foo’)
//    map json
//    Insert into FooStream;
    @Test
    public void testCreatingJmsSubscriptionJsonMapping() {
        Subscription subscription = Subscription.Subscribe(
                Transport.transport("jms").
                option("topic", "foo"));

        subscription.map(Mapping.format("json"));

        subscription.insertInto("FooStream");

        ExecutionPlan.executionPlan("test").addSubscription(subscription);

    }

//    subscribe jms options(topic  ‘foo’)
//    map text “regex1[1]” , “regex2[3]”
//    options (	regex1  “(\w+)\s(\w+)\s(\w+)\s(\w+)”,
//    regex2  “(\w+)\s(\w+)\s(\w+)\s(\w+)”)
//    insert into FooStream;
    @Test
    public void testCreatingJmsSubscriptionTextMapping() {
        Subscription subscription = Subscription.Subscribe(
                Transport.transport("jms").
                option("topic", "foo"));

        subscription.map(Mapping.format("text").
                map("regex1[1]").
                map("regex2[3]").
                option("regex1", "(\\w+)\\s(\\w+)\\s(\\w+)\\s(\\w+)").
                option("regex2", "(\\w+)\\s(\\w+)\\s(\\w+)\\s(\\w+)")
        );

        subscription.insertInto("FooStream");

        ExecutionPlan.executionPlan("test").addSubscription(subscription);

    }

}
