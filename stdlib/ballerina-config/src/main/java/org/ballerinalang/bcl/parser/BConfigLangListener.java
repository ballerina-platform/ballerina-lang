/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.bcl.parser;

import org.ballerinalang.toml.antlr4.TomlBaseListener;
import org.ballerinalang.toml.antlr4.TomlParser;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * A TOML parser listener implementation for parsing Ballerina config files.
 *
 * @since 0.965.0
 */
public class BConfigLangListener extends TomlBaseListener {

    private static final String CONFIG_KEY_SEPARATOR = ".";

    private Map<String, String> configEntries;
    private String currentTableHeader;
    private String currentKey;
    private String currentValue;

    public BConfigLangListener(Map<String, String> configEntries) {
        this.configEntries = configEntries;
    }

    @Override
    public void enterStdTable(TomlParser.StdTableContext context) {
        currentTableHeader = context.key().getText();
    }

    @Override
    public void exitKeyVal(TomlParser.KeyValContext context) {
        currentKey = null; // Reset current key once the config entry is added to the map
    }

    @Override
    public void enterKey(TomlParser.KeyContext context) {
        currentKey = context.getText();
    }

    @Override
    public void enterBasicString(TomlParser.BasicStringContext context) {
        currentValue = context.basicChar().stream().map(x -> x.getText()).collect(Collectors.joining());
    }

    @Override
    public void enterLiteralString(TomlParser.LiteralStringContext context) {
        currentValue = context.LITERALCHAR().stream().map(x -> x.getText()).collect(Collectors.joining());
    }

    @Override
    public void enterMlBasicString(TomlParser.MlBasicStringContext context) {
        currentValue = context.mlBasicBody().mlBasicChar().stream().map(x -> x.getText()).collect(Collectors.joining());
    }

    @Override
    public void enterMlLiteralString(TomlParser.MlLiteralStringContext context) {
        currentValue = context.mlLiteralBody().MLLITERALCHAR().stream().map(x -> x.getText()).collect(
                Collectors.joining());
    }

    @Override
    public void enterInteger(TomlParser.IntegerContext context) {
        currentValue = context.getText();
    }

    @Override
    public void enterFloatingPoint(TomlParser.FloatingPointContext context) {
        currentValue = context.getText();
    }

    @Override
    public void enterBool(TomlParser.BoolContext context) {
        currentValue = context.getText();
    }

    @Override
    public void exitVal(TomlParser.ValContext context) {
        String configKey;

        if (currentTableHeader != null) {
            configKey = currentTableHeader + CONFIG_KEY_SEPARATOR + currentKey;
        } else {
            configKey = currentKey;
        }

        configEntries.put(configKey, currentValue);
        currentValue = null; // Reset the current value once the config entry is added to the map
    }
}
