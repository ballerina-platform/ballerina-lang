/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.extensions.parser;

import org.ballerinalang.langserver.compiler.LSCompilerException;
import org.ballerinalang.langserver.compiler.TreeUtil;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;

import java.util.concurrent.CompletableFuture;

public class BallerinaParserServiceImpl implements BallerinaParserService {

    @Override
    public CompletableFuture<ParserReply> parseContent(ParserRequest request) {
        ParserReply reply = new ParserReply();
        try {
            reply.setModel(TreeUtil.getTreeForContent(request.getContent()));
            reply.setParseSuccess(true);
        } catch (LSCompilerException | JSONGenerationException e) {
            reply.setParseSuccess(false);
        }
        return CompletableFuture.supplyAsync(() -> reply);
    }
}
