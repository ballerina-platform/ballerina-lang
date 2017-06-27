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

package org.wso2.siddhi.core.transport;

import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.stream.AttributeMapping;
import org.wso2.siddhi.core.stream.input.source.PassThroughSourceMapper;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.util.List;

/**
 * Event Mapper implementation to handle pass-through scenario where user does not need to do any mapping.
 */
@Extension(
        name = "trpPassThrough",
        namespace = "sourceMapper",
        description = "Pass-through mapper passed events (Event[]) through without any mapping or modifications.",
        examples = @Example(
                syntax = "@source(type='tcp', @map(type='passThrough'),\n" +
                        "define stream BarStream (symbol string, price float, volume long);",
                description = "In this example BarStream uses passThrough inputmapper which passes the " +
                        "received Siddhi event directly without any transformation into source."
        )
)
public class TestTrpPassThroughSourceMapper extends PassThroughSourceMapper {

    @Override
    public void init(StreamDefinition streamDefinition, OptionHolder optionHolder, List<AttributeMapping>
            attributeMappingList, ConfigReader configReader) {

        if (attributeMappingList.get(0).getTransportMapping() == null) {
            throw new SiddhiAppCreationException("1st custom mapping is not trp!");
        }
        super.init(streamDefinition, optionHolder, attributeMappingList, configReader);
    }


}
