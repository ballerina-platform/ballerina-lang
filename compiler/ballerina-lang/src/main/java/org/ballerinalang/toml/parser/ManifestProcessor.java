/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.toml.parser;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.ballerinalang.toml.model.Manifest;
import org.ballerinalang.toml.util.TomlProcessor;

import java.io.IOException;
import java.io.InputStream;

/**
 * Manifest Processor which processes the toml file parsed and populate the Manifest POJO.
 *
 * @since 0.964
 */
public class ManifestProcessor {

    /**
     * Get the char stream of the content from file.
     *
     * @param fileName path of the toml file
     * @return manifest object
     * @throws IOException exception if the file cannot be found
     */
    public static Manifest parseTomlContentFromFile(String fileName) throws IOException {
        ANTLRFileStream in = new ANTLRFileStream(fileName);
        return getManifest(in);
    }

    /**
     * Get the char stream from string content.
     *
     * @param content toml file content as a string
     * @return manifest object
     */
    public static Manifest parseTomlContentFromString(String content) {
        ANTLRInputStream in = new ANTLRInputStream(content);
        return getManifest(in);
    }

    /**
     * Get the char stream from inputstream.
     *
     * @param inputStream inputstream of the toml file content
     * @return manifest object
     */
    public static Manifest parseTomlContentAsStream(InputStream inputStream) {
        ANTLRInputStream in = null;
        try {
            in = new ANTLRInputStream(inputStream);
        } catch (IOException ignore) {
        }
        return getManifest(in);
    }

    /**
     * Get the manifest object by passing the ballerina toml file.
     *
     * @param charStream toml file content as a char stream
     * @return manifest object
     */
    private static Manifest getManifest(CharStream charStream) {
        Manifest manifest = new Manifest();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new ManifestBuildListener(manifest), TomlProcessor.parseTomlContent(charStream));
        return manifest;
    }
}
