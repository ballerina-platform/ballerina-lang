/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.formatter.core.options;

import org.ballerinalang.formatter.core.FormatterException;

import java.nio.file.Path;
import java.util.Map;

import static org.ballerinalang.formatter.core.FormatterUtils.getFormattingConfigurations;

/**
 * A model for formatting options that could be passed onto the formatter.
 */
public class FormattingOptions {

    private final IndentFormattingOptions indentFormattingOptions;
    private final WrappingFormattingOptions wrappingFormattingOptions;
    private final BraceFormattingOptions braceFormattingOptions;
    private final FunctionDeclFormattingOptions functionDeclFormattingOptions;
    private final FunctionCallFormattingOptions functionCallFormattingOptions;
    private final IfStatementFormattingOptions ifStatementFormattingOptions;
    private final SpacingFormattingOptions spacingFormattingOptions;
    private final ForceFormattingOptions forceFormattingOptions;
    private final ImportFormattingOptions importFormattingOptions;
    private final QueryFormattingOptions queryFormattingOptions;

    private FormattingOptions(IndentFormattingOptions indentFormattingOptions,
                              WrappingFormattingOptions wrappingFormattingOptions,
                              BraceFormattingOptions braceFormattingOptions,
                              FunctionDeclFormattingOptions functionDeclFormattingOptions,
                              FunctionCallFormattingOptions functionCallFormattingOptions,
                              IfStatementFormattingOptions ifStatementFormattingOptions,
                              SpacingFormattingOptions spacingFormattingOptions,
                              ForceFormattingOptions forceFormattingOptions,
                              ImportFormattingOptions importFormattingOptions,
                              QueryFormattingOptions queryFormattingOptions) {
        this.indentFormattingOptions = indentFormattingOptions;
        this.wrappingFormattingOptions = wrappingFormattingOptions;
        this.braceFormattingOptions = braceFormattingOptions;
        this.functionDeclFormattingOptions = functionDeclFormattingOptions;
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

    public FunctionDeclFormattingOptions functionDeclFormattingOptions() {
        return functionDeclFormattingOptions;
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
        private FunctionDeclFormattingOptions functionDeclFormattingOptions =
                FunctionDeclFormattingOptions.builder().build();
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
                    functionDeclFormattingOptions, functionCallFormattingOptions, ifStatementFormattingOptions,
                    spacingFormattingOptions, forceFormattingOptions, importFormattingOptions, queryFormattingOptions);
        }

        public FormattingOptions build(Path root, Object data) throws FormatterException {
            if (!(data instanceof Map)) {
                return build();
            }
            Map<String, Object> balTomlConfigurations = (Map<String, Object>) data;
            Object path = balTomlConfigurations.get("configPath");
            if (path == null) {
                return build();
            }
            Map<String, Object> configurations = getFormattingConfigurations(root, path.toString());

            for (Map.Entry<String, Object> entry : configurations.entrySet()) {
                String key = entry.getKey();
                Object value = entry.getValue();
                if (!(value instanceof Map)) {
                    continue;
                }
                Map<String, Object> configs = (Map<String, Object>) value;
                switch (key) {
                    case "indent":
                        indentFormattingOptions = IndentFormattingOptions.builder().build(configs);
                        break;
                    case "wrapping":
                        wrappingFormattingOptions = WrappingFormattingOptions.builder().build(configs);
                        break;
                    case "braces":
                        braceFormattingOptions = BraceFormattingOptions.builder().build(configs);
                        break;
                    case "methodDeclaration":
                        functionDeclFormattingOptions = FunctionDeclFormattingOptions.builder().build(configs);
                        break;
                    case "methodCall":
                        functionCallFormattingOptions = FunctionCallFormattingOptions.builder().build(configs);
                        break;
                    case "ifStatement":
                        ifStatementFormattingOptions = IfStatementFormattingOptions.builder().build(configs);
                        break;
                    case "query":
                        queryFormattingOptions = QueryFormattingOptions.builder().build(configs);
                        break;
                    case "spacing":
                        spacingFormattingOptions = SpacingFormattingOptions.builder().build(configs);
                        break;
                    case "import":
                        importFormattingOptions = ImportFormattingOptions.builder().build(configs);
                        break;
                    default:
                        break;
                }
            }
            return build();
        }
    }

}
