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

public  class Token extends NonTerminalNode{
private Node leadingTrivia;
private Node trailingTrivia;

public Token(STNode node, int position, NonTerminalNode parent) {
super(node, position, parent);
}

public Node leadingTrivia() {
if (leadingTrivia != null) {
return leadingTrivia;
}
leadingTrivia = node.childInBucket(0).createFacade(getChildPosition(0), this);
return leadingTrivia;
}
public Node trailingTrivia() {
if (trailingTrivia != null) {
return trailingTrivia;
}
trailingTrivia = node.childInBucket(1).createFacade(getChildPosition(1), this);
return trailingTrivia;
}

public Node childInBucket(int bucket) {
switch (bucket) {
case 0:
return leadingTrivia();
case 1:
return trailingTrivia();
}
return null;
}
}
