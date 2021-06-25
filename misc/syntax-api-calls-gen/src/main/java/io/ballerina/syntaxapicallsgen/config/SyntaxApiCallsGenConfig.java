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

package io.ballerina.syntaxapicallsgen.config;

import com.google.gson.Gson;
import io.ballerina.syntaxapicallsgen.SyntaxApiCallsGen;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Base configuration file format.
 *
 * @since 2.0.0
 */
public class SyntaxApiCallsGenConfig {
    public static final String SYNTAX_TREE_DESCRIPTOR_FILE = "api_gen_syntax_tree_descriptor.json";

    private final File templateFile;
    private final Formatter formatter;
    private final int formatterTabStart;
    private final long parserTimeout;
    private final boolean ignoreMinutiae;
    private final Parser parser;

    private SyntaxApiCallsGenConfig(Builder builder) {
        this.templateFile = builder.templateFile;
        this.formatter = builder.formatter;
        this.formatterTabStart = builder.formatterTabStart;
        this.parserTimeout = builder.parserTimeout;
        this.ignoreMinutiae = builder.ignoreMinutiae;
        this.parser = builder.parser;
    }

    /**
     * Read the template file specified in the configuration.
     *
     * @return The content of the template file.
     */
    public String readTemplateFile() throws IOException {
        return Files.readString(templateFile.toPath(), Charset.defaultCharset());
    }

    /**
     * Whether to use a template file.
     */
    public boolean useTemplate() {
        return templateFile != null;
    }

    /**
     * The formatter that will be used to format the final source code.
     * Currently {@code Formatter.DEFAULT}, {@code Formatter.VARIABLE} or
     * {@code Formatter.NONE} are the used formatters.
     * The {@code Formatter.DEFAULT} will be used if this is not set.
     * <p>
     * {@code Formatter.DEFAULT} will format nested method calls.
     * {@code Formatter.VARIABLE} will format variable assigned method calls.
     * {@code Formatter.NONE} will not format the result.
     */
    public Formatter formatter() {
        return formatter;
    }

    /**
     * The tab position for the formatter to start.
     * The formatter will output code assuming that this is the tab position of input source.
     */
    public int formatterTabStart() {
        return formatterTabStart;
    }

    /**
     * Parser to use. The parser should be dependent on the context of the code snippet.
     * Currently {@code Parser.EXPRESSION}, {@code Parser.STATEMENT}, and {@code Parser.MODULE}
     * are implemented. Default if not set is {@code Parser.MODULE}.
     * <p>
     * {@code Parser.EXPRESSION} will parse assuming the snippet is an expression.
     * {@code Parser.STATEMENT} will parse assuming the snippet is an statement.
     * {@code Parser.MODULE} will parser assuming the snippet is a file.
     */
    public Parser parser() {
        return parser;
    }

    /**
     * Timeout (in milliseconds) for the parser to wait for parsing.
     * If this exceeds, the parser will exit.
     */
    public long parserTimeout() {
        return parserTimeout;
    }

    /**
     * Whether to ignore minutiae nodes and only to generate code for other nodes.
     */
    public boolean ignoreMinutiae() {
        return ignoreMinutiae;
    }

    /**
     * Get the node children config json specified in the configurations.
     * Throws an error if not found or invalid format.
     *
     * @return Parsed content of the children json file.
     */
    public Map<String, List<String>> readChildNamesJson() {
        ClassLoader classLoader = SyntaxApiCallsGen.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(SYNTAX_TREE_DESCRIPTOR_FILE);
        Gson gson = new Gson();
        Objects.requireNonNull(inputStream, "File open failed");
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        SyntaxTreeDescriptor syntaxTreeDescriptor = gson.fromJson(reader, SyntaxTreeDescriptor.class);
        return syntaxTreeDescriptor.getChildNames();
    }

    /**
     * Available parsers.
     * Will activate {@link io.ballerina.syntaxapicallsgen.parser.ExpressionParser},
     * {@link io.ballerina.syntaxapicallsgen.parser.StatementParser} or
     * {@link io.ballerina.syntaxapicallsgen.parser.ModuleParser}.
     *
     * @since 2.0.0
     */
    public enum Parser {
        EXPRESSION, STATEMENT, MODULE
    }

    /**
     * Available formatters.
     * Will activate {@link io.ballerina.syntaxapicallsgen.formatter.NoFormatter},
     * {@link io.ballerina.syntaxapicallsgen.formatter.DefaultFormatter} or
     * {@link io.ballerina.syntaxapicallsgen.formatter.VariableFormatter}.
     *
     * @since 2.0.0
     */
    public enum Formatter {
        NONE, DEFAULT, VARIABLE
    }

    /**
     * The builder for the config.
     *
     * @since 2.0.0
     */
    public static class Builder {
        private File templateFile;
        private Formatter formatter;
        private int formatterTabStart;
        private long parserTimeout;
        private boolean ignoreMinutiae;
        private Parser parser;

        public Builder() {
            this.formatterTabStart = 0;
            this.parserTimeout = 10000;
            this.ignoreMinutiae = false;
        }

        /**
         * Set the template file to be used.
         * If not set, no template will be used.
         */
        public Builder templateFile(File templateFile) {
            this.templateFile = templateFile;
            return Builder.this;
        }

        /**
         * Set the formatter to use.
         * If not set, {@code Formatter.DEFAULT} will be used.
         */
        public Builder formatter(Formatter formatter) {
            this.formatter = formatter;
            return Builder.this;
        }

        /**
         * Set the formatter tab start.
         * If not set, {@code 0} will be used.
         */
        public Builder formatterTabStart(int formatterTabStart) {
            this.formatterTabStart = formatterTabStart;
            return Builder.this;
        }

        /**
         * Set the parser to use.
         * If not set, {@code Parser.MODULE} will be used.
         */
        public Builder parser(Parser parser) {
            this.parser = parser;
            return Builder.this;
        }

        /**
         * Set the timeout for the parser (in milliseconds).
         * If not set, {@code 10000} will be used.
         */
        public Builder parserTimeout(long parserTimeout) {
            this.parserTimeout = parserTimeout;
            return Builder.this;
        }

        /**
         * Set whether to ignore minutiae.
         * Default is {@code false}.
         */
        public Builder ignoreMinutiae(boolean ignoreMinutiae) {
            this.ignoreMinutiae = ignoreMinutiae;
            return Builder.this;
        }

        /**
         * Build the configuration object.
         */
        public SyntaxApiCallsGenConfig build() {
            this.parser = Objects.requireNonNullElse(this.parser, Parser.MODULE);
            this.formatter = Objects.requireNonNullElse(this.formatter, Formatter.DEFAULT);
            return new SyntaxApiCallsGenConfig(this);
        }
    }
}
