/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.syntaxapicallsgen.formatter;

import io.ballerina.syntaxapicallsgen.config.SyntaxApiCallsGenConfig;
import io.ballerina.syntaxapicallsgen.segment.Segment;

import java.io.IOException;

/**
 * Formatter that inserts the default formatter output in a template.
 * Template is defined via configs.
 *
 * @since 2.0.0
 */
public class TemplateFormatter extends SegmentFormatter {
    private static final String TAB_CHAR = "    ";

    private final SegmentFormatter baseFormatter;
    private final String baseTemplate;
    private final int indent;

    public TemplateFormatter(SegmentFormatter baseFormatter, String baseTemplate, int indent) {
        this.baseFormatter = baseFormatter;
        this.baseTemplate = baseTemplate;
        this.indent = indent;
    }

    /**
     * Create the formatter via config.
     *
     * @param config Configuration object.
     * @return Created formatter.
     */
    public static TemplateFormatter fromConfig(SyntaxApiCallsGenConfig config) throws IOException {
        String template = config.readTemplateFile();
        int indent = config.formatterTabStart();
        return new TemplateFormatter(SegmentFormatter.getInternalFormatter(config), template, indent);
    }

    @Override
    public String format(Segment segment) {
        String nodeString = baseFormatter.format(segment);
        String[] lines = nodeString.split(System.lineSeparator());
        String indentedLines = String.join(System.lineSeparator()
                + TAB_CHAR.repeat(indent), lines);
        return String.format(baseTemplate, indentedLines);
    }
}
