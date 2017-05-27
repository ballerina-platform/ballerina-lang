/*
 * Copyright (c)  2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.siddhi.core.util.transport;

import org.wso2.siddhi.core.event.ComplexEvent;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.NoSuchAttributeException;
import org.wso2.siddhi.query.api.definition.StreamDefinition;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Template builder used by {@link org.wso2.siddhi.core.stream.output.sink.SinkMapper} to generate custom payload.
 */
public class TemplateBuilder {
    private static final Pattern DYNAMIC_PATTERN = Pattern.compile("(\\{\\{[^{}]*}})|[{}]");
    private MessageFormat messageFormat;

    public TemplateBuilder(StreamDefinition streamDefinition, String template) {
        this.messageFormat = parse(streamDefinition, template);
    }

    public static Map<String, String> convert(Event event, Map<String, TemplateBuilder> converterMap) {
        Map<String, String> mapped = new HashMap<String, String>();
        for (Map.Entry<String, TemplateBuilder> entry : converterMap.entrySet()) {
            mapped.put(entry.getKey(), entry.getValue().build(event));
        }
        return mapped;
    }

    public static String[] convert(Event event, TemplateBuilder[] templateBuilders) {
        String[] mapped = new String[templateBuilders.length];
        int i = 0;
        for (TemplateBuilder templateBuilder : templateBuilders) {
            mapped[i] = templateBuilder.build(event);
            i++;
        }
        return mapped;
    }

    public String build(Event event) {
        return messageFormat.format(event.getData());
    }

    public String build(ComplexEvent complexEvent) {
        return messageFormat.format(complexEvent.getOutputData());
    }

    private MessageFormat parse(StreamDefinition streamDefinition, String template) {
        // note: currently we do not support arbitrary data to be mapped with dynamic options
        List<String> attributes = Arrays.asList(streamDefinition.getAttributeNameArray());
        StringBuffer result = new StringBuffer();
        Matcher m = DYNAMIC_PATTERN.matcher(template);
        while (m.find()) {
            if (m.group(1) != null) {
                int attrIndex = attributes.indexOf(m.group(1).replaceAll("\\p{Ps}", "").replaceAll("\\p{Pe}", ""));
                if (attrIndex >= 0) {
                    m.appendReplacement(result, String.format("{%s}", attrIndex));
                } else {
                    throw new NoSuchAttributeException(String.format("Attribute : %s does not exist in %s.",
                                                                     m.group(1), streamDefinition));
                }
            } else {
                m.appendReplacement(result, "'" + m.group() + "'");
            }
        }
        m.appendTail(result);
        return new MessageFormat(result.toString());
    }
}
