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
import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.PackageNode;
import org.ballerinalang.model.tree.ServiceNode;
import org.ballerinalang.util.diagnostic.Diagnostic;
import org.ballerinalang.util.diagnostic.DiagnosticLog;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangExpression;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangListConstructorExpr;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangRecordLiteral;
import org.wso2.ballerinalang.compiler.tree.expressions.BLangSimpleVarRef;

import java.util.ArrayList;
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

    @Override
    public void init(DiagnosticLog diagnosticLog) {
        this.dLog = diagnosticLog;
        this.resourceSummaryList = new ArrayList<>();
        this.openAPISummaryList = new ArrayList<>();
        this.openAPIComponentSummary = new OpenAPIComponentSummary();
    }

    @Override
    public void process(ServiceNode serviceNode, List<AnnotationAttachmentNode> annotations) {
        AnnotationAttachmentNode annotation = null;
        List<String> tags = new ArrayList<>();
        List<String> operations = new ArrayList<>();
        this.openAPIComponentSummary = new OpenAPIComponentSummary();
        String contractURI = null;

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

                    if (field.getKind() == NodeKind.RECORD_LITERAL_KEY_VALUE) {
                        BLangRecordLiteral.BLangRecordKeyValue keyValue =
                                (BLangRecordLiteral.BLangRecordKeyValue) field;
                        keyExpr = keyValue.getKey();
                        valueExpr = keyValue.getValue();
                    } else {
                        BLangSimpleVarRef varRef = (BLangSimpleVarRef) field;
                        keyExpr = varRef;
                        valueExpr = varRef;
                    }

                    if (keyExpr instanceof BLangSimpleVarRef) {
                        BLangSimpleVarRef contract = (BLangSimpleVarRef) keyExpr;
                        String key = contract.getVariableName().getValue();
                        if (key.equals(Constants.CONTRACT)) {
                            if (valueExpr instanceof BLangLiteral) {
                                BLangLiteral value = (BLangLiteral) valueExpr;
                                if (value.getValue() instanceof String) {
                                    contractURI = (String) value.getValue();
                                } else {
                                    dLog.logDiagnostic(Diagnostic.Kind.ERROR, annotation.getPosition(),
                                            "Contract path should be applied as a string value");
                                }
                            }
                        } else if (key.equals(Constants.TAGS)) {
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
                                                    "Tags should be applied as string values");
                                        }
                                    }
                                }
                            }
                        } else if (key.equals(Constants.OPERATIONS)) {
                            if (valueExpr instanceof BLangListConstructorExpr) {
                                BLangListConstructorExpr bLangListConstructorExpr =
                                        (BLangListConstructorExpr) valueExpr;
                                for (BLangExpression bLangExpression : bLangListConstructorExpr.getExpressions()) {
                                    if (bLangExpression instanceof BLangLiteral) {
                                        BLangLiteral expression = (BLangLiteral) bLangExpression;
                                        if (expression.getValue() instanceof String) {
                                            operations.add((String) expression.getValue());
                                        } else {
                                            dLog.logDiagnostic(Diagnostic.Kind.ERROR, annotation.getPosition(),
                                                    "Operations should be applied as string values");
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (contractURI != null) {
                try {
                    OpenAPI openAPI = ValidatorUtil.parseOpenAPIFile(contractURI);
                    ValidatorUtil.summarizeResources(this.resourceSummaryList, serviceNode);
                    ValidatorUtil.summarizeOpenAPI(this.openAPISummaryList, openAPI, this.openAPIComponentSummary);
                    ValidatorUtil.validateOpenApiAgainstResources(serviceNode, tags, operations,
                            this.resourceSummaryList, this.openAPISummaryList, this.openAPIComponentSummary, dLog);
                    ValidatorUtil.validateResourcesAgainstOpenApi(tags, operations, this.resourceSummaryList,
                            this.openAPISummaryList, this.openAPIComponentSummary, dLog);
                } catch (OpenApiValidatorException e) {
                    dLog.logDiagnostic(Diagnostic.Kind.ERROR, annotation.getPosition(),
                            e.getMessage());
                }
            }
        }
    }

    @Override
    public void process(PackageNode packageNode) {
        // Collect endpoints throughout the package.
    }
}
