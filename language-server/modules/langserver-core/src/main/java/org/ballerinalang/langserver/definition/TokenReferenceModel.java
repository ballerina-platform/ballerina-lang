/*
 * Copyright (c) 2019, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ballerinalang.langserver.definition;

import org.antlr.v4.runtime.Token;
import org.ballerinalang.model.elements.PackageID;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;

/**
 * Token Reference model represents details about a captured token location including the package as well as the source.
 *
 * @since 0.991.0
 */
public class TokenReferenceModel {
    private Token token;
    private PackageID packageID;
    private String cUnitName;
    private Range range;


    public TokenReferenceModel(Token token, PackageID packageID, String cUnitName) {
        this.token = token;
        this.packageID = packageID;
        this.cUnitName = cUnitName;
        Position start = new Position(token.getLine(), token.getCharPositionInLine());
        Position end = new Position(token.getLine(), start.getCharacter() + token.getText().length());
        this.range = new Range(start, end);
    }

    public Token getToken() {
        return token;
    }

    public PackageID getPackageID() {
        return packageID;
    }

    public String getcUnitName() {
        return cUnitName;
    }

    public Range getRange() {
        return range;
    }
}
