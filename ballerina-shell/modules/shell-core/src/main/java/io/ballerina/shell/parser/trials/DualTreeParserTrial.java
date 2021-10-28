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

package io.ballerina.shell.parser.trials;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.shell.parser.TrialTreeParser;

import java.util.Collection;

/**
 * Trial which is run with and without trailing semicolon.
 *
 * @since 2.0.0
 */
public abstract class DualTreeParserTrial extends TreeParserTrial {
    private static final String SEMICOLON = ";";

    public DualTreeParserTrial(TrialTreeParser parentParser) {
        super(parentParser);
    }

    @Override
    public Collection<Node> parse(String source) throws ParserTrialFailedException {
        try {
            return parseSource(source);
        } catch (ParserTrialFailedException e) {
            if (source.endsWith(SEMICOLON)) {
                return parseSource(source.substring(0, source.length() - 1));
            }
            throw e;
        }
    }

    public abstract Collection<Node> parseSource(String source) throws ParserTrialFailedException;
}
