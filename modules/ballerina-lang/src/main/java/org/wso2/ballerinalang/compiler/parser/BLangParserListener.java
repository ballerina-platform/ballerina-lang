/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.parser;

import org.ballerinalang.model.tree.PackageNode;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParser;
import org.wso2.ballerinalang.compiler.parser.antlr4.BallerinaParserBaseListener;

/**
 * @since 0.94
 */
public class BLangParserListener extends BallerinaParserBaseListener {

    private BLangPackageBuilder pkgBuilder;

    public BLangParserListener(PackageNode pkgNode) {
        this.pkgBuilder = new BLangPackageBuilder(pkgNode);
    }
    
    @Override
    public void enterFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx) {
        
    }
    
    @Override 
    public void exitFunctionDefinition(BallerinaParser.FunctionDefinitionContext ctx) {
        
    }
    
    @Override 
    public void exitCallableUnitSignature(BallerinaParser.CallableUnitSignatureContext ctx) { 
        
    }
    
    @Override
    public void enterParameterList(BallerinaParser.ParameterListContext ctx) { 
        
    }
    
    @Override 
    public void exitParameter(BallerinaParser.ParameterContext ctx) {
        
    }
    
    @Override 
    public void exitValueTypeName(BallerinaParser.ValueTypeNameContext ctx) { 
        this.pkgBuilder.addValueType(ctx.getChild(0).getText());
    }
    
}
