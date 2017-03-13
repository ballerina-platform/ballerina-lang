/**
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import ASTFactory from "./../ast/ballerina-ast-factory";

class BLangModelBuilder {

    constructor(){
        this.astRoot = ASTFactory.createBallerinaAstRoot();
    }

    createPackageDeclaration(packageName, whitespaceTokens){
        var packageDeclaration = ASTFactory.createPackageDefinition({packageName: packageName});
        packageDeclaration.setWhitespaceTokens(whitespaceTokens);
        this.astRoot.setPackageDefinition(packageDeclaration);
    }

    createImportDeclaration(packageName, whitespaceTokens){
        var importDeclaration = ASTFactory.createImportDeclaration({packageName: packageName});
        importDeclaration.setWhitespaceTokens(whitespaceTokens);
        this.astRoot.addImport(importDeclaration);
    }

    getASTRoot(){
        return this.astRoot;
    }
}

export default BLangModelBuilder;