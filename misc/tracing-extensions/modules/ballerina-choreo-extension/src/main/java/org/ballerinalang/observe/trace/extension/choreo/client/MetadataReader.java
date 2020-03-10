/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
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

package org.ballerinalang.observe.trace.extension.choreo.client;

import org.ballerinalang.observe.trace.extension.choreo.BallerinaPackageResourceReader;
import org.ballerinalang.observe.trace.extension.choreo.logging.LogFactory;
import org.ballerinalang.observe.trace.extension.choreo.logging.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Properties;

/**
 * Read AST metadata from a Ballerina program package.
 */
public class MetadataReader {
    private static final Logger LOGGER = LogFactory.getLogger();

    private static final String AST_DATA_FILE_PATH = "ast/ast.json";
    private static final String AST_META_FILE_PATH = "ast/meta.properties";
    public static final String PROGRAM_HASH_KEY = "PROGRAM_HASH";

    private final InputStream astDataInputStream;
    private String astData = null;
    private Properties props = new Properties();

    public MetadataReader() throws IOException {
        InputStream inputStream = BallerinaPackageResourceReader.getResourceAsStream(AST_DATA_FILE_PATH);
        if (Objects.isNull(inputStream)) {
            throw new FileNotFoundException("Sequence diagram information cannot be found in the binary");
        }
        this.astDataInputStream = inputStream;

        try (InputStream metaFileStream = BallerinaPackageResourceReader.getResourceAsStream(AST_META_FILE_PATH)) {
            props.load(metaFileStream);
        }
    }

    public String getAstData() {
        if (Objects.isNull(astData)) {
            synchronized (this) {
                try {
                    astData = readString(astDataInputStream);
                    astDataInputStream.close();
                } catch (IOException e) {
                    LOGGER.error("Error reading AST data: " + e.getMessage());
                }
            }
        }

        return astData;
    }

    public String getAstHash() {
        String programHash = props.getProperty(PROGRAM_HASH_KEY);

        if (Objects.isNull(programHash)) {
            String hashString = Integer.toString(getAstData().hashCode());
            props.setProperty("PROGRAM_HASH", hashString);
            return hashString;
        }

        return programHash;
    }

    private static String readString(InputStream inputStream) throws IOException {
        BufferedReader buffIn = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder everything = new StringBuilder();
        String line;
        while ((line = buffIn.readLine()) != null) {
            everything.append(line);
        }
        return everything.toString();
    }
}
