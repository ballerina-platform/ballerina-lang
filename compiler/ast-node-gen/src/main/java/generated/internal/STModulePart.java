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
package generated.internal;
import generated.facade.*;

public  class STModulePart extends STNode{
public final STNode importList;
public final STNode memberList;
public final STToken eofToken;

public STModulePart(SyntaxKind kind , STNode importList, STNode memberList, STToken eofToken){
super(kind );
this.importList = importList;
this.memberList = memberList;
this.eofToken = eofToken;
this.bucketCount = 3;
this.childBuckets = new STNode[3];
this.addChildNode(importList, 0);
this.addChildNode(memberList, 1);
this.addChildNode(eofToken, 2);
}

public STModulePart(SyntaxKind kind, int width , STNode importList, STNode memberList, STToken eofToken) {
super(kind, width );
this.importList = importList;
this.memberList = memberList;
this.eofToken = eofToken;
this.bucketCount = 3;
this.childBuckets = new STNode[3];
this.addChildNode(importList, 0);
this.addChildNode(memberList, 1);
this.addChildNode(eofToken, 2);
}


public Node createFacade(int position, NonTerminalNode parent) {
return new ModulePart(this, position, parent);
}
}
