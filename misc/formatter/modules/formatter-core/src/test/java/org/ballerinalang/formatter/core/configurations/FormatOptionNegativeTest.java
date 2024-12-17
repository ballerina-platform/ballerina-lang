/*
 *  Copyright (c) 2024, WSO2 LLC. (https://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.formatter.core.configurations;

import org.ballerinalang.formatter.core.FormatterException;
import org.ballerinalang.formatter.core.options.BraceFormattingOptions;
import org.ballerinalang.formatter.core.options.BraceStyle;
import org.ballerinalang.formatter.core.options.FormatSection;
import org.ballerinalang.formatter.core.options.FunctionCallFormattingOptions;
import org.ballerinalang.formatter.core.options.FunctionDefFormattingOptions;
import org.ballerinalang.formatter.core.options.IfStatementFormattingOptions;
import org.ballerinalang.formatter.core.options.ImportFormattingOptions;
import org.ballerinalang.formatter.core.options.IndentFormattingOptions;
import org.ballerinalang.formatter.core.options.QueryFormattingOptions;
import org.ballerinalang.formatter.core.options.SpacingFormattingOptions;
import org.ballerinalang.formatter.core.options.WrappingFormattingOptions;
import org.ballerinalang.formatter.core.options.WrappingMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Test formatting options invalid configurations.
 *
 * @since 2201.9.0
 */
public class FormatOptionNegativeTest {

    @Test(expectedExceptions = FormatterException.class,
            expectedExceptionsMessageRegExp = "invalid Brace Option: serviceBraceStyle")
    public void testInvalidBracesOptions() throws FormatterException {
        Map<String, Object> configs = new HashMap<>() {{
            put("classBraceStyle", "NewLine");
            put("functionBraceStyle", "EndOfLine");
            put("serviceBraceStyle", "NewLine");
        }};
        BraceFormattingOptions.builder().build(configs);
    }

    @Test(expectedExceptions = FormatterException.class,
            expectedExceptionsMessageRegExp = "invalid function call formatting option: parametersInLine")
    public void testInvalidFunctionCallOptions() throws FormatterException {
        Map<String, Object> configs = new HashMap<>() {{
            put("argumentsWrap", "Wrap");
            put("newLineAfterLeftParen", true);
            put("alignMultilineArguments", false);
            put("rightParenOnNewLine", true);
            put("parametersInLine", true);
        }};
        FunctionCallFormattingOptions.builder().build(configs);
    }

    @Test(expectedExceptions = FormatterException.class,
            expectedExceptionsMessageRegExp = "invalid function definition formatting option: parametersInLine")
    public void testInvalidFunctionDefinitionOptions() throws FormatterException {
        Map<String, Object> configs = new HashMap<>() {{
            put("parametersWrap", "Wrap");
            put("newLineAfterLeftParen", true);
            put("alignMultilineParameters", false);
            put("rightParenOnNewLine", true);
            put("parametersInLine", true);
        }};
        FunctionDefFormattingOptions.builder().build(configs);
    }

    @Test(expectedExceptions = FormatterException.class,
            expectedExceptionsMessageRegExp = "invalid if statement formatting option: conditionOnSameLine")
    public void testInvalidIfStatementOptions() throws FormatterException {
        Map<String, Object> configs = new HashMap<>() {{
            put("elseOnNewLine", true);
            put("conditionOnSameLine", true);
        }};
        IfStatementFormattingOptions.builder().build(configs);
    }

    @Test(expectedExceptions = FormatterException.class,
            expectedExceptionsMessageRegExp = "invalid import formatting option: randomizeImports")
    public void testInvalidImportOptions() throws FormatterException {
        Map<String, Object> configs = new HashMap<>() {{
            put("groupImports", true);
            put("sortImports", true);
            put("randomizeImports", true);
        }};
        ImportFormattingOptions.builder().build(configs);
    }

    @Test(expectedExceptions = FormatterException.class,
            expectedExceptionsMessageRegExp = "invalid indent formatting option: indentFunctions")
    public void testInvalidIndentOptions() throws FormatterException {
        Map<String, Object> configs = new HashMap<>() {{
            put("indentSize", -2);
            put("continuationIndentSize", 4);
            put("indentFunctions", false);
        }};
        IndentFormattingOptions.builder().build(configs);
    }

    @Test(expectedExceptions = FormatterException.class,
            expectedExceptionsMessageRegExp = "invalid query formatting option: queryOnSameLine")
    public void testInvalidQueryOptions() throws FormatterException {
        Map<String, Object> configs = new HashMap<>() {{
            put("alignMultiLineQueries", true);
            put("queryOnSameLine", false);
        }};
        QueryFormattingOptions.builder().build(configs);
    }

    @Test(expectedExceptions = FormatterException.class,
            expectedExceptionsMessageRegExp = "invalid spacing formatting option: spaceAroundTuple")
    public void testInvalidSpacingOptions() throws FormatterException {
        Map<String, Object> configs = new HashMap<>() {{
            put("spaceAfterTypeCast", true);
            put("spaceAroundRecordBraces", true);
            put("spaceAroundTuple", true);
        }};
        SpacingFormattingOptions.builder().build(configs);
    }

    @Test(expectedExceptions = FormatterException.class,
            expectedExceptionsMessageRegExp = "invalid wrapping formatting option: wrapSimpleObjects")
    public void testInvalidWrappingOptions() throws FormatterException {
        Map<String, Object> configs = new HashMap<>() {{
            put("maxLineLength", 120);
            put("simpleBlocksInOneLine", true);
            put("simpleFunctionsInOneLine", true);
            put("wrapSimpleObjects", true);
        }};
        WrappingFormattingOptions.builder().build(configs);
    }

    @Test(expectedExceptions = FormatterException.class, expectedExceptionsMessageRegExp = "invalid Brace style:.*")
    public void testInvalidBraceStyle() throws FormatterException {
        BraceStyle.fromString("NEWLINE");
    }

    @Test(expectedExceptions = FormatterException.class, expectedExceptionsMessageRegExp = "invalid wrapping method:.*")
    public void testInvalidWrappingMethod() throws FormatterException {
        WrappingMethod.fromString("WRAP");
    }

    @Test(expectedExceptions = FormatterException.class, expectedExceptionsMessageRegExp = "invalid format section:.*")
    public void testInvalidFormatSection() throws FormatterException {
        FormatSection.fromString("service");
    }
}
