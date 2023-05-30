/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package io.ballerinalang.compiler.parser.test.tree;

import io.ballerina.compiler.syntax.tree.ModulePartNode;
import io.ballerina.compiler.syntax.tree.QueryExpressionNode;
import io.ballerina.compiler.syntax.tree.SyntaxTree;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.nio.file.Paths;

/**
 * This class tests the deprecated syntax APIs.
 *
 * @since 2201.7.0
 */
public class DeprecatedSyntaxAPITest extends AbstractSyntaxTreeAPITest {

    @Test
    public void testQueryExprDeprecatedSelectClauseAPI() {
        SyntaxTree syntaxTree = parseFile("query_expr_deprecated_api_test.bal");
        ModulePartNode modulePart = syntaxTree.rootNode();

        QueryExpressionNode queryExpressionNode = (QueryExpressionNode) modulePart.findToken(64)
                .parent().parent();
        Assert.assertNotNull(queryExpressionNode.selectClause());

        queryExpressionNode = (QueryExpressionNode) modulePart.findToken(113).parent().parent();
        try {
            queryExpressionNode.selectClause();
            Assert.fail("exception not thrown");
        } catch (IllegalStateException e) {
            Assert.assertEquals(e.getMessage(), "select-clause not found");
        }
    }

    @Override
    protected SyntaxTree parseFile(String sourceFileName) {
        return super.parseFile(Paths.get("deprecated-syntax-api-test").resolve(sourceFileName));
    }
}
