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
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.ballerinalang.launcher.toml.model.Proxy;
import org.ballerinalang.launcher.toml.util.TomlProcessor;

import java.io.IOException;

/**
 * Proxy Processor which processes the proxy configuration toml file parsed and populate the Proxy POJO
 */
public class ProxyProcessor {

    /**
     * Get the char stream of the content from file
     *
     * @param fileName path of the toml file
     * @return charstream object
     * @throws IOException exception if the file cannot be found
     */
    public static Proxy parseTomlContentFromFile(String fileName) throws IOException {
        ANTLRFileStream in = new ANTLRFileStream(fileName);
        return getProxyConfig(in);
    }

    /**
     * Get the char stream from string content
     *
     * @param content toml file content as a string
     * @return charstream object
     */
    public static Proxy parseTomlContentFromString(String content) {
        ANTLRInputStream in = new ANTLRInputStream(content);
        return getProxyConfig(in);
    }

    /**
     * Get the proxy config object by passing the proxy configuration toml file
     *
     * @param charStream toml file content as a char stream
     * @return proxy config object
     */
    public static Proxy getProxyConfig(CharStream charStream) {
        Proxy proxy = new Proxy();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new ProxyBuildListener(proxy), TomlProcessor.parseTomlContent(charStream));
        return proxy;
    }
}
