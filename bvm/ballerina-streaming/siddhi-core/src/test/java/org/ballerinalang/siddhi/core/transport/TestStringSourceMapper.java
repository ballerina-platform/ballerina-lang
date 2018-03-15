/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.siddhi.core.transport;

import org.ballerinalang.siddhi.annotation.Example;
import org.ballerinalang.siddhi.annotation.Extension;
import org.ballerinalang.siddhi.core.stream.input.source.PassThroughSourceMapper;

/**
 * Test String Mapper implementation to is used for testing purposes.
 */
@Extension(
        name = "testString",
        namespace = "sourceMapper",
        description = "testString mapper passed string events through without any mapping or modifications.",
        examples = @Example(
                syntax = "@source(type='tcp', @map(type='testString'),\n" +
                        "define stream BarStream (symbol string, price float, volume long);",
                description = "In this example BarStream uses passThrough string inputmapper which passes the " +
                        "received string directly without any transformation into source."
        )
)
public class TestStringSourceMapper extends PassThroughSourceMapper {
    @Override
    public Class[] getSupportedInputEventClasses() {
        return new Class[]{String.class};
    }
}
