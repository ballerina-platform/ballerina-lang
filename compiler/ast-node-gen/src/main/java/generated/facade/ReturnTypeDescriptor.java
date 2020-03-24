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

public  class ReturnTypeDescriptor extends NonTerminalNode{
private Token returnsKeyword;
private Node annotation;
private Node type;

public ReturnTypeDescriptor(STNode node, int position, NonTerminalNode parent) {
super(node, position, parent);
}

public Token returnsKeyword() {
if (returnsKeyword != null) {
return returnsKeyword;
}
returnsKeyword = createToken(0);
return returnsKeyword;
}
public Node annotation() {
if (annotation != null) {
return annotation;
}
annotation = node.childInBucket(1).createFacade(getChildPosition(1), this);
return annotation;
}
public Node type() {
if (type != null) {
return type;
}
type = node.childInBucket(2).createFacade(getChildPosition(2), this);
return type;
}

public Node childInBucket(int bucket) {
switch (bucket) {
case 0:
return returnsKeyword();
case 1:
return annotation();
case 2:
return type();
}
return null;
}
}
