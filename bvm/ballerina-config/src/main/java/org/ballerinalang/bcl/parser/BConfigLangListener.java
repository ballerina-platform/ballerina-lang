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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * A TOML parser listener implementation for parsing Ballerina config files.
 *
 * @since 0.966.0
 */
public class BConfigLangListener extends TomlBaseListener {

    private static final String ENCRYPTED_FIELD_REGEX = "@encrypted:\\{(.*)\\}";
    private static final String ENV_VARIABLE_REGEX = "@env:\\{([a-zA-Z_]+[a-zA-Z0-9_]*)\\}";
    private static final String CONFIG_KEY_SEPARATOR = ".";
    private static final Pattern ENV_VAR_PATTERN = Pattern.compile(ENV_VARIABLE_REGEX);

    private BConfig configEntries;
    private String currentTableHeader;
    private StringBuilder currentKey;
    private Object currentValue;
    private boolean hasEncryptedFields;

    public BConfigLangListener(BConfig configEntries) {
        this.configEntries = configEntries;
    }

    @Override
    public void exitStdTable(TomlParser.StdTableContext context) {
        currentTableHeader = currentKey.toString();
        currentKey = null;
    }

    @Override
    public void enterKey(TomlParser.KeyContext context) {
        currentKey = new StringBuilder();
    }

    @Override
    public void exitKeyVal(TomlParser.KeyValContext context) {
        currentKey = null; // Reset current key once the config entry is added to the map
    }

    @Override
    public void enterDotSep(TomlParser.DotSepContext context) {
        currentKey.append(context.getText());
    }

    @Override
    public void enterQuotedKey(TomlParser.QuotedKeyContext context) {
        currentKey = currentKey.append(context.basicString().basicStringValue().getText());
    }

    @Override
    public void enterUnquotedKey(TomlParser.UnquotedKeyContext context) {
        currentKey = currentKey.append(context.getText());
    }

    @Override
    public void enterBasicString(TomlParser.BasicStringContext context) {
        String stringVal = context.basicStringValue().getText();
        currentValue = getResolvedStringValue(stringVal);
    }

    @Override
    public void enterLiteralString(TomlParser.LiteralStringContext context) {
        String stringVal = context.LITERALCHAR().stream().map(x -> x.getText()).collect(Collectors.joining());
        currentValue = getResolvedStringValue(stringVal);
    }

    @Override
    public void enterMlBasicString(TomlParser.MlBasicStringContext context) {
        String stringVal = context.mlBasicBody().mlBasicChar().stream().map(x -> x.getText()).collect(
                Collectors.joining());
        currentValue = getResolvedStringValue(stringVal);
    }

    @Override
    public void enterMlLiteralString(TomlParser.MlLiteralStringContext context) {
        String stringVal = context.mlLiteralBody().MLLITERALCHAR().stream().map(x -> x.getText()).collect(
                Collectors.joining());
        currentValue = getResolvedStringValue(stringVal);
    }

    @Override
    public void enterDecInt(TomlParser.DecIntContext context) {
        if (!(context.getParent() instanceof TomlParser.FloatIntPartContext)) {
            try {
                currentValue = Long.parseLong(context.getText());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("invalid decimal int value: " + context.getText());
            }
        }
    }

    @Override
    public void enterBinInt(TomlParser.BinIntContext context) {
        try {
            currentValue = Long.parseLong(context.getText(), 2);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid binary int value: " + context.getText());
        }
    }

    @Override
    public void enterOctInt(TomlParser.OctIntContext context) {
        try {
            currentValue = Long.parseLong(context.getText(), 8);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid octal int value: " + context.getText());
        }
    }

    @Override
    public void enterHexInt(TomlParser.HexIntContext context) {
        try {
            currentValue = Long.parseLong(context.getText(), 16);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid hexadecimal int value: " + context.getText());
        }
    }

    @Override
    public void enterFloatingPoint(TomlParser.FloatingPointContext context) {
        try {
            currentValue = Double.parseDouble(context.getText());
            context.exitRule(this);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("invalid float value: " + context.getText());
        }
    }

    @Override
    public void enterBool(TomlParser.BoolContext context) {
        currentValue = Boolean.parseBoolean(context.getText());
    }

    @Override
    public void enterTable(TomlParser.TableContext context) {

    }

    @Override
    public void exitVal(TomlParser.ValContext context) {
        String configKey;

        if (currentTableHeader != null) {
            configKey = currentTableHeader + CONFIG_KEY_SEPARATOR + currentKey.toString();
        } else {
            configKey = currentKey.toString();
        }

        configEntries.addConfiguration(configKey, currentValue);
        currentValue = null; // Reset the current value once the config entry is added to the map
    }

    @Override
    public void exitToml(TomlParser.TomlContext context) {
        configEntries.setHasEncryptedValues(hasEncryptedFields);
    }

    private String resolveEnvVariables(String config) {
        Matcher envVarMatcher = ENV_VAR_PATTERN.matcher(config);

        if (!envVarMatcher.find()) {
            return config;
        }

        String value = System.getenv(envVarMatcher.group(1));

        return value != null ? value : config;
    }

    private String getResolvedStringValue(String value) {
        if (value.matches(ENCRYPTED_FIELD_REGEX)) {
            hasEncryptedFields = true;
        } else {
            return resolveEnvVariables(value);
        }
        return value;
    }
}
