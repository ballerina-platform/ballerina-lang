/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package io.ballerina;

import io.ballerina.compiler.syntax.tree.BasicLiteralNode;
import io.ballerina.compiler.syntax.tree.NamedArgumentNode;
import io.ballerina.compiler.syntax.tree.NodeVisitor;
import io.ballerina.compiler.syntax.tree.ParenthesizedArgList;
import io.ballerina.compiler.syntax.tree.PositionalArgumentNode;
import io.ballerina.compiler.syntax.tree.QualifiedNameReferenceNode;
import io.ballerina.compiler.syntax.tree.SimpleNameReferenceNode;
import io.ballerina.compiler.syntax.tree.Token;

/**
 * Visitor to find the base url of endpoint.
 *
 * @since 2.0.0
 */
public class UrlFinder extends NodeVisitor {

    private String token;

    public String getToken() {

        return token;
    }

    @Override
    public void visit(NamedArgumentNode namedArgumentNode) {

        namedArgumentNode.expression().accept(this);

    }

    @Override
    public void visit(PositionalArgumentNode positionalArgumentNode) {

        positionalArgumentNode.expression().accept(this);

    }

    @Override
    public void visit(SimpleNameReferenceNode simpleNameReferenceNode) {

        simpleNameReferenceNode.name().accept(this);

    }

    @Override
    public void visit(QualifiedNameReferenceNode qualifiedNameReferenceNode) {

        qualifiedNameReferenceNode.identifier().accept(this);

    }

    @Override
    public void visit(BasicLiteralNode basicLiteralNode) {

        basicLiteralNode.literalToken().accept(this);
    }

    @Override
    public void visit(ParenthesizedArgList parenthesizedArgList) {

        parenthesizedArgList.arguments().forEach(functionArgumentNode -> functionArgumentNode.accept(this));

    }

    @Override
    public void visit(Token token) {

        this.token = token.text();
    }
}
