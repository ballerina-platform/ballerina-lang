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

public  class STBracedExpression extends STExpression{
public final STToken leadingComma;
public final STNode accessModifier;
public final STNode type;
public final STNode paramName;
public final STToken equal;
public final STNode expr;

public STBracedExpression(SyntaxKind kind , STToken leadingComma, STNode accessModifier, STNode type, STNode paramName, STToken equal, STNode expr){
super(kind );
this.leadingComma = leadingComma;
this.accessModifier = accessModifier;
this.type = type;
this.paramName = paramName;
this.equal = equal;
this.expr = expr;
this.bucketCount = 6;
this.childBuckets = new STNode[6];
this.addChildNode(leadingComma, 0);
this.addChildNode(accessModifier, 1);
this.addChildNode(type, 2);
this.addChildNode(paramName, 3);
this.addChildNode(equal, 4);
this.addChildNode(expr, 5);
}

public STBracedExpression(SyntaxKind kind, int width , STToken leadingComma, STNode accessModifier, STNode type, STNode paramName, STToken equal, STNode expr) {
super(kind, width );
this.leadingComma = leadingComma;
this.accessModifier = accessModifier;
this.type = type;
this.paramName = paramName;
this.equal = equal;
this.expr = expr;
this.bucketCount = 6;
this.childBuckets = new STNode[6];
this.addChildNode(leadingComma, 0);
this.addChildNode(accessModifier, 1);
this.addChildNode(type, 2);
this.addChildNode(paramName, 3);
this.addChildNode(equal, 4);
this.addChildNode(expr, 5);
}


public Node createFacade(int position, NonTerminalNode parent) {
return new BracedExpression(this, position, parent);
}
}
