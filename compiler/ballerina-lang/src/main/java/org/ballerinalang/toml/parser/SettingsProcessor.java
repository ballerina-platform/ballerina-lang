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
import org.ballerinalang.toml.model.Settings;
import org.ballerinalang.toml.util.TomlProcessor;

import java.io.IOException;

/**
 * SettingHeaders Processor which processes the settings toml file parsed and populate the SettingHeaders POJO.
 *
 * @since 0.964
 */
public class SettingsProcessor {

    /**
     * Get the char stream of the content from file.
     *
     * @param fileName path of the toml file
     * @return charstream object
     * @throws IOException exception if the file cannot be found
     */
    public static Settings parseTomlContentFromFile(String fileName) throws IOException {
        ANTLRFileStream in = new ANTLRFileStream(fileName);
        return getSettings(in);
    }

    /**
     * Get the char stream from string content.
     *
     * @param content toml file content as a string
     * @return charstream object
     */
    public static Settings parseTomlContentFromString(String content) {
        ANTLRInputStream in = new ANTLRInputStream(content);
        return getSettings(in);
    }

    /**
     * Get the settings config object by passing the settings toml file.
     *
     * @param charStream toml file content as a char stream
     * @return settings object
     */
    private static Settings getSettings(CharStream charStream) {
        Settings settings = new Settings();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new SettingsBuildListener(settings), TomlProcessor.parseTomlContent(charStream));
        return settings;
    }
}
