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

public  class STBinaryExpression extends STExpression{
public final STNode lhsExpr;
public final STNode operator;
public final STNode rhsExpr;

public STBinaryExpression(SyntaxKind kind , STNode lhsExpr, STNode operator, STNode rhsExpr){
super(kind );
this.lhsExpr = lhsExpr;
this.operator = operator;
this.rhsExpr = rhsExpr;
this.bucketCount = 3;
this.childBuckets = new STNode[3];
this.addChildNode(lhsExpr, 0);
this.addChildNode(operator, 1);
this.addChildNode(rhsExpr, 2);
}

public STBinaryExpression(SyntaxKind kind, int width , STNode lhsExpr, STNode operator, STNode rhsExpr) {
super(kind, width );
this.lhsExpr = lhsExpr;
this.operator = operator;
this.rhsExpr = rhsExpr;
this.bucketCount = 3;
this.childBuckets = new STNode[3];
this.addChildNode(lhsExpr, 0);
this.addChildNode(operator, 1);
this.addChildNode(rhsExpr, 2);
}


public Node createFacade(int position, NonTerminalNode parent) {
return new BinaryExpression(this, position, parent);
}
}
