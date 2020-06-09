/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.openapi.validator;

import io.swagger.v3.oas.models.OpenAPI;
import org.ballerinalang.compiler.plugins.AbstractCompilerPlugin;
import org.ballerinalang.compiler.plugins.SupportedAnnotationPackages;
import org.ballerinalang.model.tree.AnnotationAttachmentNode;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.SourceDirectoryManager;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Compiler plugin for ballerina OpenAPI/service validator.
 */
@SupportedAnnotationPackages(value = {"ballerina/openapi"})
public class OpenAPIValidatorPlugin extends AbstractCompilerPlugin {
    private DiagnosticLog dLog = null;
    private List<ResourceSummary> resourceSummaryList;
    private List<OpenAPIPathSummary> openAPISummaryList;
    private OpenAPIComponentSummary openAPIComponentSummary;
    private CompilerContext compilerContext;

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dLog = diagnosticLog;
        this.resourceSummaryList = new ArrayList<>();
        this.openAPISummaryList = new ArrayList<>();
        this.openAPIComponentSummary = new OpenAPIComponentSummary();
    }

    @Override
    public void setCompilerContext(CompilerContext context) {
        this.compilerContext = context;
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        AnnotationAttachmentNode annotation = null;
        List<String> tags = new ArrayList<>();
        List<String> operations = new ArrayList<>();
        List<String> excludeTags = new ArrayList<>();
        List<String> excludeOperations = new ArrayList<>();
        this.openAPIComponentSummary = new OpenAPIComponentSummary();
        String contractURI = null;
        Boolean failOnErrors = true;
        Diagnostic.Kind kind;

        for (AnnotationAttachmentNode ann : annotations) {
            if (Constants.PACKAGE.equals(ann.getPackageAlias().getValue())
                    && Constants.ANNOTATION_NAME.equals(ann.getAnnotationName().getValue())) {
                annotation = ann;
            }
        }

        if (annotation != null) {
            if (annotation.getExpression() instanceof BLangRecordLiteral) {
                BLangRecordLiteral recordLiteral = (BLangRecordLiteral) annotation.getExpression();
                for (BLangRecordLiteral.RecordField field : recordLiteral.getFields()) {
                    BLangExpression keyExpr;
                    BLangExpression valueExpr;

                    if (field.isKeyValueField()) {
                        BLangRecordLiteral.BLangRecordKeyValueField keyValue =
                                (BLangRecordLiteral.BLangRecordKeyValueField) field;
                        keyExpr = keyValue.getKey();
                        valueExpr = keyValue.getValue();
                    } else {
                        BLangRecordLiteral.BLangRecordVarNameField varNameField =
                                (BLangRecordLiteral.BLangRecordVarNameField) field;
                        keyExpr = varNameField;
                        valueExpr = varNameField;
                    }

                    if (keyExpr instanceof BLangSimpleVarRef) {
                        BLangSimpleVarRef contract = (BLangSimpleVarRef) keyExpr;
                        String key = contract.getVariableName().getValue();
                        if (key.equals(Constants.CONTRACT)) {
                            if (valueExpr instanceof BLangLiteral) {
                                BLangLiteral value = (BLangLiteral) valueExpr;
                                SourceDirectoryManager sourceDirectoryManager = SourceDirectoryManager.getInstance(
                                        compilerContext);
                                Path sourceDir = sourceDirectoryManager.getSourceDirectory().getPath();
                                Path pkg = Paths.get(serviceNode.getPosition().getSource().getPackageName());
                                Path filePath = Paths.get((pkg.toString().equals(".") ? "" : pkg.toString()),
                                        serviceNode.getPosition().getSource().getCompilationUnitName().replaceAll(
                                                "\\w*\\.bal", "").replaceAll("^/+", ""));

                                String projectDir = filePath.toString().
                                        contains(sourceDir.toString().replaceAll("^/+", "")) ?
                                        sourceDir.toString() :
                                        Paths.get(sourceDir.toString(), "src", filePath.toString()).toString();
                                if (value.getValue() instanceof String) {
                                    String userUri = (String) value.getValue();

                                    File file = null;
                                    if (userUri.contains(projectDir)) {
                                        file = new File(userUri);
                                    } else {
                                        try {
                                            file = new File(Paths.get(projectDir, userUri).toRealPath().toString());
                                        } catch (IOException e) {
                                            contractURI = Paths.get(userUri).toString();
                                        }
                                    }
                                    if (file != null && file.exists()) {
                                        contractURI = file.getAbsolutePath();
                                    }
                                } else {
                                    dLog.logDiagnostic(Diagnostic.Kind.ERROR, annotation.getPosition(),
                                            "Contract path should be applied as a string value");
                                }
                            }
                        } else if (key.equals(Constants.TAGS)) {
                            extractValues(annotation, tags, valueExpr, "Tags should be applied as string values");
                        } else if (key.equals(Constants.OPERATIONS)) {
                            extractValues(annotation, operations, valueExpr,
                                    "Operations should be applied as string values");
                        }  else if (key.equals(Constants.FAILONERRORS)) {
                            if (valueExpr instanceof BLangLiteral) {
                                BLangLiteral value = (BLangLiteral) valueExpr;
                                if (value.getValue() instanceof Boolean) {
                                    failOnErrors = (Boolean) value.getValue();
                                } else {
                                    dLog.logDiagnostic(Diagnostic.Kind.ERROR, annotation.getPosition(),
                                            "FailOnErrors should be applied as boolean values");
                                }
                            }
                        } else if (key.equals(Constants.EXCLUDETAGS)) {
                            extractValues(annotation, excludeTags, valueExpr,
                                    "ExcludeTags should be applied as string values");
                        } else if (key.equals(Constants.EXCLUDEOPERATIONS)) {
                            extractValues(annotation, excludeOperations, valueExpr,
                                    "ExcludeOperations should be applied as string values");
                        }
                    }
                }
            }

            if (failOnErrors) {
                kind = Diagnostic.Kind.ERROR;
            } else {
                kind = Diagnostic.Kind.WARNING;
            }

//            Checking both tags and excludeTags include same tags and operations
            if (!Collections.disjoint(tags, excludeTags)) {
                dLog.logDiagnostic(Diagnostic.Kind.WARNING, annotation.getPosition(),
                        ErrorMessages.tagFilterEnable());
                excludeTags.clear();

            } else if (!Collections.disjoint(operations, excludeOperations)) {
                dLog.logDiagnostic(Diagnostic.Kind.WARNING, annotation.getPosition(),
                        ErrorMessages.operationFilterEnable());
                excludeOperations.clear();
            }

            if (contractURI != null) {
                try {
                    OpenAPI openAPI = ValidatorUtil.parseOpenAPIFile(contractURI);
                    ValidatorUtil.summarizeResources(this.resourceSummaryList, serviceNode);
                    ValidatorUtil.summarizeOpenAPI(this.openAPISummaryList, openAPI, this.openAPIComponentSummary);
                    ValidatorUtil.validateOpenApiAgainstResources(serviceNode, tags, operations, kind, excludeTags,
                            excludeOperations,
                            this.resourceSummaryList, this.openAPISummaryList,
                            this.openAPIComponentSummary, dLog);
                    ValidatorUtil.validateResourcesAgainstOpenApi(tags, operations, kind, excludeTags,
                            excludeOperations,
                            this.resourceSummaryList,
                            this.openAPISummaryList, this.openAPIComponentSummary,
                            dLog);
                } catch (OpenApiValidatorException e) {
                    dLog.logDiagnostic(Diagnostic.Kind.ERROR, annotation.getPosition(),
                            e.getMessage());
                }
            }
        }
    }

    private void extractValues(AnnotationAttachmentNode annotation, List<String> tags,
                               BLangExpression valueExpr, String s) {
        if (valueExpr instanceof BLangListConstructorExpr) {
            BLangListConstructorExpr bLangListConstructorExpr =
                    (BLangListConstructorExpr) valueExpr;
            for (BLangExpression bLangExpression : bLangListConstructorExpr.getExpressions()) {
                if (bLangExpression instanceof BLangLiteral) {
                    BLangLiteral expression = (BLangLiteral) bLangExpression;
                    if (expression.getValue() instanceof String) {
                        tags.add((String) expression.getValue());
                    } else {
                        dLog.logDiagnostic(Diagnostic.Kind.ERROR, annotation.getPosition(),
                                s);
                    }
                }
            }
        }
    }

    @Override
    public void process(PackageNode packageNode) {
        // Collect endpoints throughout the package.
    }
}
