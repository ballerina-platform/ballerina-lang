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

public  class AssignmentStatement extends NonTerminalNode{
private Node varRef;
private Node equalsToken;
private Node expr;
private Node semicolonToken;

public AssignmentStatement(STNode node, int position, NonTerminalNode parent) {
super(node, position, parent);
}

public Node varRef() {
if (varRef != null) {
return varRef;
}
varRef = node.childInBucket(0).createFacade(getChildPosition(0), this);
return varRef;
}
public Node equalsToken() {
if (equalsToken != null) {
return equalsToken;
}
equalsToken = node.childInBucket(1).createFacade(getChildPosition(1), this);
return equalsToken;
}
public Node expr() {
if (expr != null) {
return expr;
}
expr = node.childInBucket(2).createFacade(getChildPosition(2), this);
return expr;
}
public Node semicolonToken() {
if (semicolonToken != null) {
return semicolonToken;
}
semicolonToken = node.childInBucket(3).createFacade(getChildPosition(3), this);
return semicolonToken;
}

public Node childInBucket(int bucket) {
switch (bucket) {
case 0:
return varRef();
case 1:
return equalsToken();
case 2:
return expr();
case 3:
return semicolonToken();
}
return null;
}
}
