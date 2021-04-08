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

package io.ballerina.syntaxapicallsgen;

import io.ballerina.compiler.syntax.tree.Node;
import io.ballerina.syntaxapicallsgen.config.QuoterConfig;
import io.ballerina.syntaxapicallsgen.formatter.SegmentFormatter;
import io.ballerina.syntaxapicallsgen.parser.QuoterParser;
import io.ballerina.syntaxapicallsgen.segment.Segment;
import io.ballerina.syntaxapicallsgen.segment.factories.NodeSegmentFactory;

/**
 * Ballerina Quoter programme main class.
 * CLI will run via QuoterCommandLine
 *
 * @since 2.0.0
 */
public class BallerinaQuoter {
    /**
     * Run the process with the given configurations.
     *
     * @param sourceCode Ballerina source code
     * @param config     Configuration object
     * @return Generated Java code
     */
    public static String generate(String sourceCode, QuoterConfig config) {
        try {
            QuoterParser parser = QuoterParser.fromConfig(config);
            NodeSegmentFactory factory = NodeSegmentFactory.fromConfig(config);
            SegmentFormatter formatter = SegmentFormatter.getFormatter(config);

            // 1. Get the syntax tree
            Node syntaxTreeNode = parser.parse(sourceCode);
            // 2. Convert tree to a segment tree
            Segment segment = factory.createNodeSegment(syntaxTreeNode);
            // 3. Format using the formatter
            return formatter.format(segment);
        } catch (QuoterException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new QuoterException("There was an Exception when parsing. Please check your code.", exception);
        }
    }
}
