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
import org.wso2.siddhi.query.api.execution.query.Query;
import org.wso2.siddhi.query.api.execution.query.input.stream.InputStream;
import org.wso2.siddhi.query.api.execution.query.output.stream.OutputStream;
import org.wso2.siddhi.query.api.execution.io.Transport;
import org.wso2.siddhi.query.api.execution.io.map.Mapping;

public class PublishQueryTestCase {

//    from FooStream
//    select *
//    publish all events email options (
//            address  "abc@test.con",
//            subject  "{{data}}-type")
//    map text 	“””
//    Hi user
//    {{data}} on {{time}}
//    ”””
//    ;

    @Test
    public void testPublishEmailWithTextMapping() {
        Query query = Query.query();
        query.from(
                InputStream.stream("FooStream")
        );
        query.publish(
                Transport.transport("email").
                        option("address", "abc@test.con").option("subject", "{{data}}-type"),
                OutputStream.OutputEventType.ALL_EVENTS,
                Mapping.format("text").map("\n" +
                        "    Hi user\n" +
                        "    {{data}} on {{time}}\n" +
                        "    "));

    }

    @Test
    public void testPublishEmailWithTextMappingCurrentEvents() {
        Query query = Query.query();
        query.from(
                InputStream.stream("FooStream")
        );
        query.publish(
                Transport.transport("email").
                        option("address", "abc@test.con").option("subject", "{{data}}-type"),
                Mapping.format("text").map("\n" +
                        "    Hi user\n" +
                        "    {{data}} on {{time}}\n" +
                        "    "));

    }

//    from FooStream
//    publish jms options ( topic ‘foo’)
//    map text;
    @Test
    public void testCreatingJmsWithTextMapping() {
        Query query = Query.query();
        query.from(
                InputStream.stream("FooStream")
        );
        query.publish(Transport.transport("jms").option("topic", "foo"), OutputStream.OutputEventType.CURRENT_EVENTS,
                Mapping.format("text")
        );

    }

}
