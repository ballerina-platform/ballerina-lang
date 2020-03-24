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

public  class STMissingToken extends STToken{
public final boolean IS_MISSING;

public STMissingToken(SyntaxKind kind , boolean IS_MISSING){
super(kind ,null,null);
this.IS_MISSING = IS_MISSING;
this.bucketCount = 1;
this.childBuckets = new STNode[1];
}

public STMissingToken(SyntaxKind kind, int width , boolean IS_MISSING) {
super(kind, width ,null,null);
this.IS_MISSING = IS_MISSING;
this.bucketCount = 1;
this.childBuckets = new STNode[1];
}


}
