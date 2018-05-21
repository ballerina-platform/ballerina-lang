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
package org.ballerinalang.toml.util;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.ballerinalang.toml.antlr4.TomlLexer;
import org.ballerinalang.toml.antlr4.TomlParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Util methods for toml processor.
 *
 * @since 0.964
 */
public class TomlProcessor {

    /**
     * Generate the proxy object by passing in the toml file.
     *
     * @param stream charstream object containing the content
     * @return proxy object
     */
    public static ParseTree parseTomlContent(CharStream stream) {
        TomlLexer lexer = new TomlLexer(stream);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        TomlParser parser = new TomlParser(tokens);
        return parser.toml();
    }

    /**
     * Populate list values.
     *
     * @param arrayValuesContext array values
     * @return list of strings
     */
    public static List<String> populateList(TomlParser.ArrayValuesContext arrayValuesContext) {
        List<String> arrayElements = new ArrayList<>();
        if (arrayValuesContext != null) {
            for (TomlParser.ArrayvalsNonEmptyContext valueContext : arrayValuesContext.arrayvalsNonEmpty()) {
                String value = valueContext.val().getText();
                if (valueContext.val().string() != null) {
                    value = valueContext.val().string().basicString().basicStringValue().getText();
                }
                arrayElements.add(value);
            }
        }
        return arrayElements;
    }

    /**
     * Get table heading of the standard table.
     *
     * @param ctx Standardtable context
     * @return table heading as a list
     */
    public static List<String> getTableHeading(TomlParser.StdTableContext ctx) {
        List<String> tableHeading = new ArrayList<>();
        TomlParser.DottedKeyContext dottedKeyContext = ctx.key().dottedKey();
        if (dottedKeyContext != null) {
            for (int i = 0; i < (dottedKeyContext.getChildCount() + 1) / 2; i++) {
                TomlParser.SimpleKeyContext simpleKeyContext = dottedKeyContext.simpleKey(i);
                TomlParser.QuotedKeyContext quotedKeyContext = simpleKeyContext.quotedKey();
                if (quotedKeyContext != null) {
                    tableHeading.add(quotedKeyContext.basicString().basicStringValue().getText());
                } else {
                    tableHeading.add(simpleKeyContext.unquotedKey().getText());
                }
            }
        } else {
            tableHeading.add(ctx.key().getText());
        }

        return tableHeading;
    }
}
