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
package org.ballerinalang.langserver.extensions.ballerina.fragment;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.langserver.compiler.ExtendedLSCompiler;
import org.ballerinalang.langserver.compiler.LSCompilerUtil;
import org.ballerinalang.langserver.compiler.common.modal.BallerinaFile;
import org.ballerinalang.langserver.compiler.exception.CompilationFailedException;
import org.ballerinalang.langserver.compiler.format.JSONGenerationException;
import org.ballerinalang.langserver.compiler.format.TextDocumentFormatUtil;
import org.ballerinalang.langserver.compiler.sourcegen.FormattingSourceGen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/**
 * Ballerina fragment service.
 *
 * @since 0.981.2
 */
public class BallerinaFragmentServiceImpl implements BallerinaFragmentService {

    private static final Logger logger = LoggerFactory.getLogger(BallerinaFragmentServiceImpl.class);

    private static final String SYNTAX_ERRORS = "syntax_errors";
    private static final String ERROR = "error";

    @Override
    public CompletableFuture<BallerinaFragmentASTResponse> ast(BallerinaFragmentASTRequest request) {
        return CompletableFuture.supplyAsync(() -> {
            BallerinaFragmentASTResponse response = new BallerinaFragmentASTResponse();
            response.setAst(parseFragment(request));
            return response;
        });
    }

    private static JsonObject parseFragment(BallerinaFragmentASTRequest sourceFragment) {
        try {
            String parsableString = getParsableString(sourceFragment);
            JsonElement jsonElement = getJsonModel(parsableString);
            if (jsonElement instanceof JsonObject) {
                JsonObject jsonModel = (JsonObject) jsonElement;
                if (jsonModel.getAsJsonArray(SYNTAX_ERRORS) != null) {
                    return jsonModel;
                }
                JsonObject jsonASTFragment = getJsonNodeForFragment(jsonModel, sourceFragment);
                return FormattingSourceGen.build(jsonASTFragment, null);
            }
        } catch (JSONGenerationException | CompilationFailedException e) {
            logger.error("Error while generating AST for fragment", e);
        }
        return null;
    }

    private static JsonObject getJsonNodeForFragment(JsonObject jsonModel, BallerinaFragmentASTRequest fragment) {
        JsonObject fragmentNode;
        JsonArray jsonArray = jsonModel.getAsJsonArray(JSONModelConstants.TOP_LEVEL_NODES);
        JsonObject rootConstruct = jsonArray.get(0).getAsJsonObject(); // 0 is package def
        switch (fragment.getExpectedNodeType()) {
            case BLangFragmentParserConstants.TOP_LEVEL_NODE:
                fragmentNode = rootConstruct;
                break;
            case BLangFragmentParserConstants.SERVICE_RESOURCE:
                fragmentNode = rootConstruct.getAsJsonArray(JSONModelConstants.RESOURCES).get(0).getAsJsonObject();
                break;
            case BLangFragmentParserConstants.CONNECTOR_ACTION:
                fragmentNode = rootConstruct.getAsJsonArray(JSONModelConstants.ACTIONS).get(0).getAsJsonObject();
                break;
            case BLangFragmentParserConstants.WORKER:
                fragmentNode = rootConstruct.getAsJsonArray(JSONModelConstants.WORKERS)
                        .get(0).getAsJsonObject();
                break;
            case BLangFragmentParserConstants.VARIABLE_REFERENCE_LIST:
                // 0 & 1 are function args and return types, 2 is the assignment statement
                JsonObject assignmentStmt = rootConstruct.getAsJsonArray(JSONModelConstants.CHILDREN)
                        .get(2).getAsJsonObject();
                // 0th child is the var ref list expression of assignment stmt
                fragmentNode = assignmentStmt.getAsJsonArray(JSONModelConstants.CHILDREN)
                        .get(0).getAsJsonObject();
                break;
            case BLangFragmentParserConstants.FIELD_DEFINITION_LIST:
                // 0th element in the fields property of the record is fieldVariable.
                fragmentNode = rootConstruct.getAsJsonObject(JSONModelConstants.TYPE_NODE)
                        .getAsJsonArray(JSONModelConstants.FIELDS).get(0).getAsJsonObject();
                break;
            case BLangFragmentParserConstants.ANON_RECORD:
                fragmentNode = jsonModel;
                break;
            case BLangFragmentParserConstants.TRANSACTION_FAILED:
            case BLangFragmentParserConstants.EXPRESSION:
            case BLangFragmentParserConstants.STATEMENT:
                // For Expression - 0th child is the var ref expression of var def stmt
                // For Statement - 0 & 1 are function args and return types, 2 is the statement came from source
                // fragment
                fragmentNode = rootConstruct.getAsJsonObject(JSONModelConstants.BODY)
                        .getAsJsonArray(JSONModelConstants.STATEMENTS).get(0).getAsJsonObject();
                break;
            case BLangFragmentParserConstants.ENDPOINT_VAR_DEF:
                fragmentNode = rootConstruct.getAsJsonArray(JSONModelConstants.ENDPOINT_NODES)
                        .get(0).getAsJsonObject();
                break;
            case BLangFragmentParserConstants.JOIN_CONDITION:
                JsonObject bodyJsonObj = rootConstruct.getAsJsonObject(JSONModelConstants.BODY);
                fragmentNode = bodyJsonObj.getAsJsonArray(JSONModelConstants.STATEMENTS).get(0).getAsJsonObject();
                break;
            case BLangFragmentParserConstants.ARGUMENT_PARAMETER:
                fragmentNode = rootConstruct.getAsJsonArray(JSONModelConstants.PARAMETERS)
                        .get(0)
                        .getAsJsonObject();
                break;
            case BLangFragmentParserConstants.RETURN_PARAMETER:
                fragmentNode = rootConstruct.getAsJsonArray(JSONModelConstants.RETURN_PARAMETERS)
                        .get(0)
                        .getAsJsonObject();
                break;
            default:
                fragmentNode = new JsonObject();
                fragmentNode.addProperty(ERROR, "cannot find node for given fragment");
        }
        return fragmentNode;
    }

    private static JsonElement getJsonModel(String source)
            throws CompilationFailedException, JSONGenerationException {
        BallerinaFile model = ExtendedLSCompiler.compileContent(source, CompilerPhase.DEFINE);
        Optional<BLangCompilationUnit> compilationUnit = model.getBLangPackage()
                .map(b -> b.getCompilationUnits().stream().filter(
                        compUnit -> LSCompilerUtil.UNTITLED_BAL.equals(compUnit.getName())
                ).findFirst().orElse(null));
        return TextDocumentFormatUtil.generateJSON(compilationUnit.orElse(null), new HashMap<>(), new HashMap<>());
    }

    private static String getParsableString(BallerinaFragmentASTRequest sourceFragment) {
        String parsableText = null;
        String source = sourceFragment.getSource();
        switch (sourceFragment.getExpectedNodeType()) {
            case BLangFragmentParserConstants.TOP_LEVEL_NODE:
                parsableText = source;
                break;
            case BLangFragmentParserConstants.SERVICE_RESOURCE:
                parsableText = getFromTemplate(BLangFragmentParserConstants.SERVICE_BODY_RESOURCE_WRAPPER, source);
                break;
            case BLangFragmentParserConstants.CONNECTOR_ACTION:
                parsableText = getFromTemplate(BLangFragmentParserConstants.CONNECTOR_BODY_ACTION_WRAPPER, source);
                break;
            case BLangFragmentParserConstants.EXPRESSION:
                parsableText = getFromTemplate(BLangFragmentParserConstants.VAR_DEF_STMT_EXPR_WRAPPER, source);
                break;
            case BLangFragmentParserConstants.WORKER:
            case BLangFragmentParserConstants.ENDPOINT_VAR_DEF:
            case BLangFragmentParserConstants.STATEMENT:
            case BLangFragmentParserConstants.ANON_RECORD:
                parsableText = getFromTemplate(BLangFragmentParserConstants.FUNCTION_BODY_STMT_WRAPPER, source);
                break;
            case BLangFragmentParserConstants.JOIN_CONDITION:
                parsableText = getFromTemplate(BLangFragmentParserConstants.FORK_JOIN_CONDITION_WRAPPER, source);
                break;
            case BLangFragmentParserConstants.ARGUMENT_PARAMETER:
                parsableText = getFromTemplate(BLangFragmentParserConstants.FUNCTION_SIGNATURE_PARAMETER_WRAPPER,
                        source);
                break;
            case BLangFragmentParserConstants.RETURN_PARAMETER:
                parsableText = getFromTemplate(BLangFragmentParserConstants.FUNCTION_SIGNATURE_RETURN_WRAPPER, source);
                break;
            case BLangFragmentParserConstants.TRANSACTION_FAILED:
                parsableText = getFromTemplate(BLangFragmentParserConstants.TRANSACTION_FAILED_RETRY_WRAPPER, source);
                break;
            case BLangFragmentParserConstants.VARIABLE_REFERENCE_LIST:
                parsableText = getFromTemplate(BLangFragmentParserConstants.VAR_REFERENCE_LIST_WRAPPER, source);
                break;
            case BLangFragmentParserConstants.FIELD_DEFINITION_LIST:
                parsableText = getFromTemplate(BLangFragmentParserConstants.RECORD_BODY_WRAPPER, source);
                break;
            default:
                parsableText = "";
        }
        return parsableText;
    }

    private static String getFromTemplate(String template, String source) {
        return template.replace(BLangFragmentParserConstants.FRAGMENT_PLACE_HOLDER, source);
    }
}
