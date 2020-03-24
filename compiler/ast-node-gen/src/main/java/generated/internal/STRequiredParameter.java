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

public  class STRequiredParameter extends STParameter{
public final STToken leadingComma;
public final STNode accessModifier;
public final STNode type;
public final STNode paramName;

public STRequiredParameter(SyntaxKind kind , STToken leadingComma, STNode accessModifier, STNode type, STNode paramName){
super(kind );
this.leadingComma = leadingComma;
this.accessModifier = accessModifier;
this.type = type;
this.paramName = paramName;
this.bucketCount = 4;
this.childBuckets = new STNode[4];
this.addChildNode(leadingComma, 0);
this.addChildNode(accessModifier, 1);
this.addChildNode(type, 2);
this.addChildNode(paramName, 3);
}

public STRequiredParameter(SyntaxKind kind, int width , STToken leadingComma, STNode accessModifier, STNode type, STNode paramName) {
super(kind, width );
this.leadingComma = leadingComma;
this.accessModifier = accessModifier;
this.type = type;
this.paramName = paramName;
this.bucketCount = 4;
this.childBuckets = new STNode[4];
this.addChildNode(leadingComma, 0);
this.addChildNode(accessModifier, 1);
this.addChildNode(type, 2);
this.addChildNode(paramName, 3);
}


public Node createFacade(int position, NonTerminalNode parent) {
return new RequiredParameter(this, position, parent);
}
}
