/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.quoter.config;

import com.google.gson.Gson;
import io.ballerina.quoter.BallerinaQuoter;

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
 */
public class QuoterConfig {
    public static final String SYNTAX_TREE_DESCRIPTOR_FILE = "syntax_tree_descriptor.json";

    private final File templateFile;
    private final boolean useTemplate;
    private final String formatterName;
    private final int formatterTabStart;
    private final long parserTimeout;
    private final boolean ignoreMinutiae;
    private final Parser parser;

    private QuoterConfig(Builder builder) {
        this.templateFile = builder.templateFile;
        this.useTemplate = builder.useTemplate;
        this.formatterName = builder.formatterName;
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

    public String formatterName() {
        return formatterName;
    }

    public int formatterTabStart() {
        return formatterTabStart;
    }

    public long parserTimeout() {
        return parserTimeout;
    }

    public boolean ignoreMinutiae() {
        return ignoreMinutiae;
    }

    public Parser parser() {
        return parser;
    }

    public boolean useTemplate() {
        return useTemplate;
    }

    /**
     * Get the node children config json specified in the configurations.
     * Throws an error if not found or invalid format.
     *
     * @return Parsed content of the children json file.
     */
    public Map<String, List<String>> readChildNamesJson() {
        ClassLoader classLoader = BallerinaQuoter.class.getClassLoader();
        InputStream inputStream = classLoader.getResourceAsStream(SYNTAX_TREE_DESCRIPTOR_FILE);
        Gson gson = new Gson();
        Objects.requireNonNull(inputStream, "File open failed");
        InputStreamReader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        SyntaxTreeDescriptor syntaxTreeDescriptor = gson.fromJson(reader, SyntaxTreeDescriptor.class);
        return syntaxTreeDescriptor.getChildNames();
    }

    /**
     *
     */
    public enum Parser {
        EXPRESSION, STATEMENT, MODULE
    }

    /**
     *
     */
    public static class Builder {
        private File templateFile;
        private boolean useTemplate;
        private String formatterName;
        private int formatterTabStart;
        private long parserTimeout;
        private boolean ignoreMinutiae;
        private Parser parser;

        public Builder() {
        }

        public Builder templateFile(File templateFile) {
            this.templateFile = templateFile;
            return Builder.this;
        }

        public Builder useTemplate(boolean useTemplate) {
            this.useTemplate = useTemplate;
            return Builder.this;
        }

        public Builder formatterName(String formatterName) {
            this.formatterName = formatterName;
            return Builder.this;
        }

        public Builder formatterTabStart(int formatterTabStart) {
            this.formatterTabStart = formatterTabStart;
            return Builder.this;
        }

        public Builder parserTimeout(long parserTimeout) {
            this.parserTimeout = parserTimeout;
            return Builder.this;
        }

        public Builder ignoreMinutiae(boolean ignoreMinutiae) {
            this.ignoreMinutiae = ignoreMinutiae;
            return Builder.this;
        }

        public Builder parser(Parser parser) {
            this.parser = parser;
            return Builder.this;
        }

        public QuoterConfig build() {
            Objects.requireNonNull(this.templateFile, "Template file must be provided.");
            Objects.requireNonNull(this.formatterName, "Formatter name must be provided.");
            Objects.requireNonNull(this.parser, "Parser type must be provided.");
            return new QuoterConfig(this);
        }
    }
}
