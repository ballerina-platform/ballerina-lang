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
package org.ballerinalang.formatter.core.options;

import org.ballerinalang.formatter.core.FormatterException;

import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static org.ballerinalang.formatter.core.FormatterUtils.getFormattingConfigurations;
import static org.ballerinalang.formatter.core.FormatterUtils.getFormattingFilePath;

/**
 * A model for formatting options that could be passed onto the formatter.
 */
public class FormattingOptions {

    private final IndentFormattingOptions indentFormattingOptions;
    private final WrappingFormattingOptions wrappingFormattingOptions;
    private final BraceFormattingOptions braceFormattingOptions;
    private final FunctionDefFormattingOptions functionDefFormattingOptions;
    private final FunctionCallFormattingOptions functionCallFormattingOptions;
    private final IfStatementFormattingOptions ifStatementFormattingOptions;
    private final SpacingFormattingOptions spacingFormattingOptions;
    private final ForceFormattingOptions forceFormattingOptions;
    private final ImportFormattingOptions importFormattingOptions;
    private final QueryFormattingOptions queryFormattingOptions;

    private FormattingOptions(IndentFormattingOptions indentFormattingOptions,
                              WrappingFormattingOptions wrappingFormattingOptions,
                              BraceFormattingOptions braceFormattingOptions,
                              FunctionDefFormattingOptions functionDefFormattingOptions,
                              FunctionCallFormattingOptions functionCallFormattingOptions,
                              IfStatementFormattingOptions ifStatementFormattingOptions,
                              SpacingFormattingOptions spacingFormattingOptions,
                              ForceFormattingOptions forceFormattingOptions,
                              ImportFormattingOptions importFormattingOptions,
                              QueryFormattingOptions queryFormattingOptions) {
        this.indentFormattingOptions = indentFormattingOptions;
        this.wrappingFormattingOptions = wrappingFormattingOptions;
        this.braceFormattingOptions = braceFormattingOptions;
        this.functionDefFormattingOptions = functionDefFormattingOptions;
        this.functionCallFormattingOptions = functionCallFormattingOptions;
        this.ifStatementFormattingOptions = ifStatementFormattingOptions;
        this.spacingFormattingOptions = spacingFormattingOptions;
        this.forceFormattingOptions = forceFormattingOptions;
        this.importFormattingOptions = importFormattingOptions;
        this.queryFormattingOptions = queryFormattingOptions;
    }

    public ForceFormattingOptions forceFormattingOptions() {
        return forceFormattingOptions;
    }

    public ImportFormattingOptions importFormattingOptions() {
        return importFormattingOptions;
    }

    public IndentFormattingOptions indentFormattingOptions() {
        return indentFormattingOptions;
    }

    public WrappingFormattingOptions wrappingFormattingOptions() {
        return wrappingFormattingOptions;
    }

    public BraceFormattingOptions braceFormattingOptions() {
        return braceFormattingOptions;
    }

    public FunctionDefFormattingOptions functionDefFormattingOptions() {
        return functionDefFormattingOptions;
    }

    public FunctionCallFormattingOptions functionCallFormattingOptions() {
        return functionCallFormattingOptions;
    }

    public IfStatementFormattingOptions ifStatementFormattingOptions() {
        return ifStatementFormattingOptions;
    }

    public SpacingFormattingOptions spacingFormattingOptions() {
        return spacingFormattingOptions;
    }

    public QueryFormattingOptions queryFormattingOptions() {
        return queryFormattingOptions;
    }

    public static FormattingOptionsBuilder builder() {
        return new FormattingOptionsBuilder();
    }

    /**
     * A builder for the {@code FormattingOptions}.
     *
     * @since 2201.3.0
     */
    public static class FormattingOptionsBuilder {

        private int tabSize = 4;
        private String wsCharacter = " ";
        private int columnLimit = 120;
        private boolean lineWrapping = false;
        private IndentFormattingOptions indentFormattingOptions = IndentFormattingOptions.builder().build();
        private WrappingFormattingOptions wrappingFormattingOptions = WrappingFormattingOptions.builder().build();
        private BraceFormattingOptions braceFormattingOptions = BraceFormattingOptions.builder().build();
        private FunctionDefFormattingOptions functionDefFormattingOptions =
                FunctionDefFormattingOptions.builder().build();
        private FunctionCallFormattingOptions functionCallFormattingOptions =
                FunctionCallFormattingOptions.builder().build();
        private IfStatementFormattingOptions ifStatementFormattingOptions =
                IfStatementFormattingOptions.builder().build();
        private SpacingFormattingOptions spacingFormattingOptions = SpacingFormattingOptions.builder().build();
        private ImportFormattingOptions importFormattingOptions = ImportFormattingOptions.builder().build();
        private QueryFormattingOptions queryFormattingOptions = QueryFormattingOptions.builder().build();
        private ForceFormattingOptions forceFormattingOptions = ForceFormattingOptions.builder().build();

        @Deprecated
        public FormattingOptions.FormattingOptionsBuilder setTabSize(int tabSize) {
            this.tabSize = tabSize;
            return this;
        }

        @Deprecated
        public FormattingOptions.FormattingOptionsBuilder setWSCharacter(String wsCharacter) {
            this.wsCharacter = wsCharacter;
            return this;
        }

        @Deprecated
        public FormattingOptions.FormattingOptionsBuilder setColumnLimit(int columnLimit) {
            this.columnLimit = columnLimit;
            return this;
        }

        @Deprecated
        public FormattingOptions.FormattingOptionsBuilder setLineWrapping(boolean lineWrapping) {
            this.lineWrapping = lineWrapping;
            return this;
        }

        public FormattingOptionsBuilder setForceFormattingOptions(ForceFormattingOptions forceFormattingOptions) {
            this.forceFormattingOptions = forceFormattingOptions;
            return this;
        }

        public FormattingOptionsBuilder setImportFormattingOptions(ImportFormattingOptions importFormattingOptions) {
            this.importFormattingOptions = importFormattingOptions;
            return this;
        }

        public FormattingOptionsBuilder setWrappingFormattingOptions(
                WrappingFormattingOptions wrappingFormattingOptions) {
            this.wrappingFormattingOptions = wrappingFormattingOptions;
            return this;
        }

        public FormattingOptions build() {
            return new FormattingOptions(indentFormattingOptions, wrappingFormattingOptions, braceFormattingOptions,
                    functionDefFormattingOptions, functionCallFormattingOptions, ifStatementFormattingOptions,
                    spacingFormattingOptions, forceFormattingOptions, importFormattingOptions, queryFormattingOptions);
        }

        public FormattingOptions build(Path root, Object formatSection) throws FormatterException {
            Optional<String> path = getFormattingFilePath(formatSection, root.toString());
            if (path.isEmpty()) {
                return build();
            }
            Map<String, Object> configurations = getFormattingConfigurations(root, path.get());
            for (Map.Entry<String, Object> entry : configurations.entrySet()) {
                Object value = entry.getValue();
                if (!(value instanceof Map<?, ?> configs)) {
                    continue;
                }
                String key = entry.getKey();
                FormatSection section = FormatSection.fromString(key);
                switch (section) {
                    case INDENT -> indentFormattingOptions = IndentFormattingOptions.builder().build(
                            (Map<String, Object>) configs);
                    case WRAPPING -> wrappingFormattingOptions = WrappingFormattingOptions.builder().build(
                            (Map<String, Object>) configs);
                    case BRACES -> braceFormattingOptions = BraceFormattingOptions.builder().build(
                            (Map<String, Object>) configs);
                    case FUNCTION_DEFINITION ->
                            functionDefFormattingOptions = FunctionDefFormattingOptions.builder().build(
                                    (Map<String, Object>) configs);
                    case FUNCTION_CALL ->
                            functionCallFormattingOptions = FunctionCallFormattingOptions.builder().build(
                                    (Map<String, Object>) configs);
                    case IF_STATEMENT ->
                            ifStatementFormattingOptions = IfStatementFormattingOptions.builder().build(
                                    (Map<String, Object>) configs);
                    case QUERY -> queryFormattingOptions = QueryFormattingOptions.builder().build(
                            (Map<String, Object>) configs);
                    case SPACING -> spacingFormattingOptions = SpacingFormattingOptions.builder().build(
                            (Map<String, Object>) configs);
                    case IMPORT -> importFormattingOptions = ImportFormattingOptions.builder().build(
                            (Map<String, Object>) configs);
                }
            }
            return build();
        }
    }

}
