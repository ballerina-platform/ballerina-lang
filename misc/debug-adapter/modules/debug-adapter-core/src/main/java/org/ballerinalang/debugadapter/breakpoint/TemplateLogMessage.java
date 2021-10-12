/*
 * Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.debugadapter.breakpoint;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represents Ballerina logpoint messages with expression interpolations.
 *
 * @since 2.0.0
 */
public class TemplateLogMessage extends LogMessage {

    private List<String> expressions;
    private List<String> interpolationResults;

    TemplateLogMessage(String message) {
        super(message);
        extractInterpolations();
    }

    @Override
    public String getMessage() {
        if (this.interpolationResults == null) {
            throw new RuntimeException("expression interpolation results are not loaded");
        }
        return this.message;
    }

    public List<String> getExpressions() {
        return this.expressions;
    }

    /**
     * Replaces the expression interpolations in the original expression, with the provided set evaluation results.
     */
    public void resolveInterpolations(List<String> evaluationResults) {
        this.interpolationResults = evaluationResults;
        Pattern pattern = Pattern.compile(INTERPOLATION_REGEX);
        Matcher matcher = pattern.matcher(message);
        AtomicInteger index = new AtomicInteger();
        this.message = matcher.replaceAll(matchResult -> evaluationResults.get(index.getAndIncrement()));
    }

    /**
     * Extracts expressions from the log point message template.
     */
    private void extractInterpolations() {
        List<String> expressions = new ArrayList<>();
        Matcher matcher = Pattern.compile(INTERPOLATION_REGEX).matcher(message);
        while (matcher.find()) {
            String expression = matcher.group();
            // Removes '${' and '}' characters from the expression.
            expressions.add(expression.substring(2, expression.length() - 1));
        }
        this.expressions = expressions;
    }
}
