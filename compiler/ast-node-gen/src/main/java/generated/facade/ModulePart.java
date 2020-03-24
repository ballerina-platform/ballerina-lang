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

public  class ModulePart extends NonTerminalNode{
private NodeList<Node> importList;
private NodeList<Node> memberList;
private Token eofToken;

public ModulePart(STNode node, int position, NonTerminalNode parent) {
super(node, position, parent);
}

public NodeList<Node> importList() {
if (importList != null) {
return importList;
}
importList = createListNode(0);
return importList;
}
public NodeList<Node> memberList() {
if (memberList != null) {
return memberList;
}
memberList = createListNode(1);
return memberList;
}
public Token eofToken() {
if (eofToken != null) {
return eofToken;
}
eofToken = createToken(2);
return eofToken;
}

public Node childInBucket(int bucket) {
switch (bucket) {
case 0:
return importList();
case 1:
return memberList();
case 2:
return eofToken();
}
return null;
}
}
