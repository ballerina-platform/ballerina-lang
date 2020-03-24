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

public  class STToken extends STNode{
public final STNode leadingTrivia;
public final STNode trailingTrivia;

public STToken(SyntaxKind kind , STNode leadingTrivia, STNode trailingTrivia){
super(kind );
this.leadingTrivia = leadingTrivia;
this.trailingTrivia = trailingTrivia;
this.bucketCount = 2;
this.childBuckets = new STNode[2];
this.addChildNode(leadingTrivia, 0);
this.addChildNode(trailingTrivia, 1);
}

public STToken(SyntaxKind kind, int width , STNode leadingTrivia, STNode trailingTrivia) {
super(kind, width );
this.leadingTrivia = leadingTrivia;
this.trailingTrivia = trailingTrivia;
this.bucketCount = 2;
this.childBuckets = new STNode[2];
this.addChildNode(leadingTrivia, 0);
this.addChildNode(trailingTrivia, 1);
}

public String toString() {
return leadingTrivia + kind.strValue + trailingTrivia;
}

public Node createFacade(int position, NonTerminalNode parent) {
return new Token(this, position, parent);
}
}
