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

public  class STReturnTypeDescriptor extends STNode{
public final STToken returnsKeyword;
public final STNode annotation;
public final STNode type;

public STReturnTypeDescriptor(SyntaxKind kind , STToken returnsKeyword, STNode annotation, STNode type){
super(kind );
this.returnsKeyword = returnsKeyword;
this.annotation = annotation;
this.type = type;
this.bucketCount = 3;
this.childBuckets = new STNode[3];
this.addChildNode(returnsKeyword, 0);
this.addChildNode(annotation, 1);
this.addChildNode(type, 2);
}

public STReturnTypeDescriptor(SyntaxKind kind, int width , STToken returnsKeyword, STNode annotation, STNode type) {
super(kind, width );
this.returnsKeyword = returnsKeyword;
this.annotation = annotation;
this.type = type;
this.bucketCount = 3;
this.childBuckets = new STNode[3];
this.addChildNode(returnsKeyword, 0);
this.addChildNode(annotation, 1);
this.addChildNode(type, 2);
}


public Node createFacade(int position, NonTerminalNode parent) {
return new ReturnTypeDescriptor(this, position, parent);
}
}
