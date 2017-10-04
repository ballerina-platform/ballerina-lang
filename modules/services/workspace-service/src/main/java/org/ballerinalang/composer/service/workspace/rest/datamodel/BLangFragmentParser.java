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
package org.ballerinalang.composer.service.workspace.rest.datamodel;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.composer.service.workspace.util.WorkspaceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerinalang.compiler.tree.BLangCompilationUnit;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * Utility for parsing BLang source fragments
 */
public class BLangFragmentParser {

    private static final Logger logger = LoggerFactory.getLogger(BLangFragmentParser.class);

    public static final String SYNTAX_ERRORS = "syntax_errors";
    public static final String ERROR = "error";

    public static String parseFragment(BLangSourceFragment sourceFragment) {
        try {
            String parsableString = getParsableString(sourceFragment);
            JsonElement jsonElement = getJsonModel(parsableString);
            if(jsonElement instanceof JsonObject){
                JsonObject jsonModel = (JsonObject)jsonElement;
                if (jsonModel.getAsJsonArray(SYNTAX_ERRORS) != null) {
                    return jsonModel.toString();
                }
                JsonObject jsonNodeForFragment = getJsonNodeForFragment(jsonModel, sourceFragment);
                return jsonNodeForFragment.toString();
            }else{
                throw new Exception("Incorrect format of the generated JSON");
            }
        } catch (Exception e) {
            logger.error("Error while parsing BLang fragment.", e);
            JsonObject errObj = new JsonObject();
            errObj.addProperty(ERROR, e.getMessage());
            return errObj.toString();
        }
    }

    protected static JsonObject getJsonNodeForFragment(JsonObject jsonModel, BLangSourceFragment fragment) {
        JsonObject fragmentNode = null;
        JsonArray jsonArray = jsonModel.getAsJsonArray(BLangJSONModelConstants.TOP_LEVEL_NODES);
        JsonObject rootConstruct = jsonArray.get(0).getAsJsonObject(); // 0 is package def
        switch (fragment.getExpectedNodeType()) {
            case BLangFragmentParserConstants.TOP_LEVEL_NODE:
                fragmentNode = rootConstruct;
                break;
            case BLangFragmentParserConstants.SERVICE_RESOURCE:
                fragmentNode = rootConstruct.getAsJsonArray(BLangJSONModelConstants.RESOURCES).get(0).getAsJsonObject();
                break;
            case BLangFragmentParserConstants.CONNECTOR_ACTION:
                fragmentNode = rootConstruct.getAsJsonArray(BLangJSONModelConstants.ACTIONS).get(0).getAsJsonObject();
                break;
            case BLangFragmentParserConstants.EXPRESSION:
                // 0 & 1 are function args and return types, 2 is the var def stmt
                JsonObject varDef = rootConstruct.getAsJsonArray(BLangJSONModelConstants.CHILDREN)
                        .get(2).getAsJsonObject();
                // 0th child is the var ref expression of var def stmt
                fragmentNode = varDef.getAsJsonArray(BLangJSONModelConstants.CHILDREN)
                        .get(1).getAsJsonObject();
                break;
            case BLangFragmentParserConstants.VARIABLE_REFERENCE_LIST:
                // 0 & 1 are function args and return types, 2 is the assignment statement
                JsonObject assignmentStmt = rootConstruct.getAsJsonArray(BLangJSONModelConstants.CHILDREN)
                        .get(2).getAsJsonObject();
                // 0th child is the var ref list expression of assignment stmt
                fragmentNode = assignmentStmt.getAsJsonArray(BLangJSONModelConstants.CHILDREN)
                        .get(0).getAsJsonObject();
                break;
            case BLangFragmentParserConstants.STATEMENT:
                // 0 & 1 are function args and return types, 2 is the statement came from source fragment
                fragmentNode = rootConstruct.getAsJsonObject(BLangJSONModelConstants.BODY)
                        .getAsJsonArray(BLangJSONModelConstants.STATEMENTS).get(0).getAsJsonObject();
                break;
            case BLangFragmentParserConstants.JOIN_CONDITION:
                fragmentNode = rootConstruct.getAsJsonArray(BLangJSONModelConstants.CHILDREN).get(2)
                        .getAsJsonObject().getAsJsonArray(BLangJSONModelConstants.CHILDREN).get(0).getAsJsonObject();
                fragmentNode.remove(BLangJSONModelConstants.CHILDREN);
                fragmentNode.remove(BLangJSONModelConstants.JOIN_PARAMETER);
                break;
            case BLangFragmentParserConstants.ARGUMENT_PARAMETER:
                fragmentNode = rootConstruct.getAsJsonArray(BLangJSONModelConstants.PARAMETERS).get(0).getAsJsonObject();
                break;
            case BLangFragmentParserConstants.RETURN_PARAMETER:
                fragmentNode = rootConstruct.getAsJsonArray(BLangJSONModelConstants.RETURN_PARAMETERS).get(0).getAsJsonObject();
                break;
            case BLangFragmentParserConstants.TRANSACTION_FAILED:
                fragmentNode = rootConstruct.getAsJsonArray(BLangJSONModelConstants.CHILDREN).get(2)
                        .getAsJsonObject().getAsJsonArray(BLangJSONModelConstants.CHILDREN).get(1)
                        .getAsJsonObject().getAsJsonArray(BLangJSONModelConstants.CHILDREN).get(0).getAsJsonObject();
                break;
            default:
                fragmentNode = new JsonObject();
                fragmentNode.addProperty(ERROR, "cannot find node for given fragment");
        }
        return fragmentNode;
    }

    protected static JsonElement getJsonModel(String source) throws IOException {
        String fileName = "untitled";
        BLangPackage model = WorkspaceUtils.getBallerinaFileForContent(fileName, source, CompilerPhase.DEFINE)
                .getBLangPackage();
        BLangCompilationUnit compilationUnit = model.getCompilationUnits().stream().
                filter(compUnit -> fileName.equals(compUnit.getName())).findFirst().get();

        try {
            return BLangFileRestService.generateJSON(compilationUnit);
        } catch (InvocationTargetException | IllegalAccessException e) {
            // This should never occur.
            throw new AssertionError("Error while serializing source to JSON.");
        }
    }

    protected static String getParsableString(BLangSourceFragment sourceFragment) {
        String parsableText = null;
        switch (sourceFragment.getExpectedNodeType()) {
            case BLangFragmentParserConstants.TOP_LEVEL_NODE:
                parsableText = sourceFragment.getSource();
                break;
            case BLangFragmentParserConstants.SERVICE_RESOURCE:
                parsableText = getFromTemplate(
                        BLangFragmentParserConstants.SERVICE_BODY_RESOURCE_WRAPPER, sourceFragment.getSource());
                break;
            case BLangFragmentParserConstants.CONNECTOR_ACTION:
                parsableText = getFromTemplate(
                        BLangFragmentParserConstants.CONNECTOR_BODY_ACTION_WRAPPER, sourceFragment.getSource());
                break;
            case BLangFragmentParserConstants.EXPRESSION:
                parsableText = getFromTemplate(
                        BLangFragmentParserConstants.VAR_DEF_STMT_EXPR_WRAPPER, sourceFragment.getSource());
                break;
            case BLangFragmentParserConstants.STATEMENT:
                parsableText = getFromTemplate(
                        BLangFragmentParserConstants.FUNCTION_BODY_STMT_WRAPPER, sourceFragment.getSource());
                break;
            case BLangFragmentParserConstants.JOIN_CONDITION:
                parsableText = getFromTemplate(
                        BLangFragmentParserConstants.FORK_JOIN_CONDITION_WRAPPER, sourceFragment.getSource());
                break;
            case BLangFragmentParserConstants.ARGUMENT_PARAMETER:
                parsableText = getFromTemplate(BLangFragmentParserConstants.FUNCTION_SIGNATURE_PARAMETER_WRAPPER,
                        sourceFragment.getSource());
                break;
            case BLangFragmentParserConstants.RETURN_PARAMETER:
                parsableText = getFromTemplate(BLangFragmentParserConstants.FUNCTION_SIGNATURE_RETURN_WRAPPER,
                        sourceFragment.getSource());
                break;
            case BLangFragmentParserConstants.TRANSACTION_FAILED:
                parsableText = getFromTemplate(BLangFragmentParserConstants.TRANSACTION_FAILED_RETRY_WRAPPER,
                        sourceFragment.getSource());
                break;
            case BLangFragmentParserConstants.VARIABLE_REFERENCE_LIST:
                parsableText = getFromTemplate(
                        BLangFragmentParserConstants.VAR_REFERENCE_LIST_WRAPPER, sourceFragment.getSource());
                break;
            default:
                parsableText = "";
        }
        return parsableText;
    }

    protected static String getFromTemplate(String template, String source) {
        return template.replace(BLangFragmentParserConstants.FRAGMENT_PLACE_HOLDER, source);
    }
}

