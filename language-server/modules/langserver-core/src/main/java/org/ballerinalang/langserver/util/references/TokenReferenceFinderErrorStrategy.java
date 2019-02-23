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
package org.ballerinalang.langserver.util.references;

import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.Token;
import org.ballerinalang.langserver.common.constants.NodeContextKeys;
import org.ballerinalang.langserver.compiler.LSContext;
import org.ballerinalang.langserver.compiler.common.LSCustomErrorStrategy;
import org.ballerinalang.model.elements.PackageID;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * When a token name is given, find the token references matching the given token name and store them in a map.
 *
 * @since 0.993.0
 */
public class TokenReferenceFinderErrorStrategy extends LSCustomErrorStrategy {
    private LSContext lsContext;
    private String tokenName;

    public TokenReferenceFinderErrorStrategy(LSContext context) {
        super(context);
        this.lsContext = context;
        this.tokenName = this.lsContext.get(NodeContextKeys.NODE_NAME_KEY);
        if (lsContext.get(NodeContextKeys.TOKEN_REFERENCES_KEY) == null) {
            lsContext.put(NodeContextKeys.TOKEN_REFERENCES_KEY, new HashMap<>());
        }
    }

    @Override
    public void reportMatch(Parser recognizer) {
        super.reportMatch(recognizer);
        Token currentToken = recognizer.getCurrentToken();
        if (currentToken.getText().equals(this.tokenName)) {
            PackageID pkgID = ((TokenReferenceFinderErrorStrategy) recognizer.getErrorHandler()).diagnosticSrc.pkgID;
            String sourceName = recognizer.getSourceName();
            TokenReferenceModel tokenReferenceModel = new TokenReferenceModel(currentToken, pkgID, sourceName);
            Map<String, Map<String, List<TokenReferenceModel>>> refMap =
                    lsContext.get(NodeContextKeys.TOKEN_REFERENCES_KEY);

            if (refMap.containsKey(pkgID.getName().getValue())) {
                Map<String, List<TokenReferenceModel>> cUnitMap = refMap.get(pkgID.getName().getValue());
                if (cUnitMap.containsKey(sourceName)) {
                    cUnitMap.get(sourceName).add(tokenReferenceModel);
                } else {
                    List<TokenReferenceModel> refList = new ArrayList<>();
                    refList.add(tokenReferenceModel);
                    cUnitMap.put(sourceName, refList);
                }
            } else {
                List<TokenReferenceModel> refList = new ArrayList<>();
                refList.add(tokenReferenceModel);
                Map<String, List<TokenReferenceModel>> cUnitMap = new HashMap<>();
                cUnitMap.put(sourceName, refList);
                refMap.put(pkgID.getName().getValue(), cUnitMap);
            }
        }
    }
}
