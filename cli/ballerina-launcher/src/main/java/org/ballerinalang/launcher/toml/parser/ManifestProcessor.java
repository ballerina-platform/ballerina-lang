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
package org.ballerinalang.launcher.toml.parser;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.ballerinalang.launcher.toml.antlr4.TomlLexer;
import org.ballerinalang.launcher.toml.antlr4.TomlParser;
import org.ballerinalang.launcher.toml.model.Manifest;

import java.io.IOException;

/**
 * Manifest Processor which processes the toml file parsed and populate the Manifest POJO
 */
public class ManifestProcessor {

    /**
     * Get the char stream of the content from file
     *
     * @param fileName path of the toml file
     * @return charstream object
     * @throws IOException exception if the file cannot be found
     */
    public static Manifest parseTomlContentFromFile(String fileName) throws IOException {
        ANTLRFileStream in = new ANTLRFileStream(fileName);
        return parseTomlContent(in);
    }

    /**
     * Get the char stream from string content
     *
     * @param content toml file content as a string
     * @return charstream object
     */
    public static Manifest parseTomlContentFromString(String content) {
        ANTLRInputStream in = new ANTLRInputStream(content);
        return parseTomlContent(in);
    }

    /**
     * Generate the manifest object by passing in the toml file
     *
     * @param stream charstream object containing the content
     * @return manifest object
     */
    private static Manifest parseTomlContent(CharStream stream){
        Manifest manifest = new Manifest();
        TomlLexer lexer = new TomlLexer(stream);

        // Get a list of matched tokens
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // Pass the tokens to the parser
        TomlParser parser = new TomlParser(tokens);
        ParseTree tree = parser.toml();

        // Walk it and attach our listener
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new ManifestBuildListener(manifest), tree);
        System.out.println(tree.toStringTree(parser));
        return manifest;

    }
}
