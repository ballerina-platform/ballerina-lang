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

package org.wso2.siddhi.core.publisher;

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

public class Converter {
    private static final Pattern DYNAMIC_PATTERN = Pattern.compile("(\\{\\{[^{}]*}})|[{}]");
    private String template;

    public Converter(StreamDefinition streamDefinition, String template) {
        this.template = parse(streamDefinition, template);
    }

    public static Map<String, String> convert(Event event, Map<String, Converter> converterMap) {
        Map<String, String> mapped = new HashMap<String, String>();
        for (Map.Entry<String, Converter> entry : converterMap.entrySet()) {
            mapped.put(entry.getKey(), entry.getValue().map(event));
        }
        return mapped;
    }

    public static String[] convert(Event event, Converter[] converters) {
        String[] mapped = new String[converters.length];
        int i = 0;
        for (Converter converter : converters) {
            mapped[i] = converter.map(event);
            i++;
        }
        return mapped;
    }

    public String map(Event event) {
        return MessageFormat.format(template, event.getData());
    }

    private String parse(StreamDefinition streamDefinition, String template) {
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
        return result.toString();
    }
}
