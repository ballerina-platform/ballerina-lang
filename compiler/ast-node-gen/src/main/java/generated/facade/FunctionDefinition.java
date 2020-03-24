/*
*  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package generated.facade;
import generated.internal.STNode;

public  class FunctionDefinition extends NonTerminalNode{
private Node visibilityQual;
private Token functionKeyword;
private Node functionName;
private Token openParenToken;
private NodeList<Node> parameters;
private Token closeParenToken;
private Node returnTypeDescriptor;
private Node functionBody;

public FunctionDefinition(STNode node, int position, NonTerminalNode parent) {
super(node, position, parent);
}

public Node visibilityQual() {
if (visibilityQual != null) {
return visibilityQual;
}
visibilityQual = node.childInBucket(0).createFacade(getChildPosition(0), this);
return visibilityQual;
}
public Token functionKeyword() {
if (functionKeyword != null) {
return functionKeyword;
}
functionKeyword = createToken(1);
return functionKeyword;
}
public Node functionName() {
if (functionName != null) {
return functionName;
}
functionName = node.childInBucket(2).createFacade(getChildPosition(2), this);
return functionName;
}
public Token openParenToken() {
if (openParenToken != null) {
return openParenToken;
}
openParenToken = createToken(3);
return openParenToken;
}
public NodeList<Node> parameters() {
if (parameters != null) {
return parameters;
}
parameters = createListNode(4);
return parameters;
}
public Token closeParenToken() {
if (closeParenToken != null) {
return closeParenToken;
}
closeParenToken = createToken(5);
return closeParenToken;
}
public Node returnTypeDescriptor() {
if (returnTypeDescriptor != null) {
return returnTypeDescriptor;
}
returnTypeDescriptor = node.childInBucket(6).createFacade(getChildPosition(6), this);
return returnTypeDescriptor;
}
public Node functionBody() {
if (functionBody != null) {
return functionBody;
}
functionBody = node.childInBucket(7).createFacade(getChildPosition(7), this);
return functionBody;
}

public Node childInBucket(int bucket) {
switch (bucket) {
case 0:
return visibilityQual();
case 1:
return functionKeyword();
case 2:
return functionName();
case 3:
return openParenToken();
case 4:
return parameters();
case 5:
return closeParenToken();
case 6:
return returnTypeDescriptor();
case 7:
return functionBody();
}
return null;
}
}
