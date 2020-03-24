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

public  class STBlockStatement extends STStatement{
public final STToken openBraceToken;
public final STNode statementList;
public final STToken closeBraceToken;

public STBlockStatement(SyntaxKind kind , STToken openBraceToken, STNode statementList, STToken closeBraceToken){
super(kind );
this.openBraceToken = openBraceToken;
this.statementList = statementList;
this.closeBraceToken = closeBraceToken;
this.bucketCount = 3;
this.childBuckets = new STNode[3];
this.addChildNode(openBraceToken, 0);
this.addChildNode(statementList, 1);
this.addChildNode(closeBraceToken, 2);
}

public STBlockStatement(SyntaxKind kind, int width , STToken openBraceToken, STNode statementList, STToken closeBraceToken) {
super(kind, width );
this.openBraceToken = openBraceToken;
this.statementList = statementList;
this.closeBraceToken = closeBraceToken;
this.bucketCount = 3;
this.childBuckets = new STNode[3];
this.addChildNode(openBraceToken, 0);
this.addChildNode(statementList, 1);
this.addChildNode(closeBraceToken, 2);
}


public Node createFacade(int position, NonTerminalNode parent) {
return new BlockStatement(this, position, parent);
}
}
