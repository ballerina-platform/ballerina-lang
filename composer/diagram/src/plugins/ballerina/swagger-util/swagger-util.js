/**
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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


import TreeUtil from 'plugins/ballerina/model/tree-util.js';

/**
 * This parser class provides means of merging a swagger JSON to a {@link ServiceDefinition} or a
 * {@link ResourceDefinition}.
 */
class SwaggerUtil {

    static merge(ast, swaggerAst, targetService) {
        swaggerAst.topLevelNodes.forEach((element) => {
            // add missing imports
            if (TreeUtil.isImport(element)) {
                if (!SwaggerUtil.hasImport(ast, element)) {
                    ast.addTopLevelNodes(element, 0);
                }
            }

            // merge service.
            if (TreeUtil.isService(element)) {
                SwaggerUtil.mergeService(targetService, element);
            }
        });

        // find the service
        // add missing annotations
        //
    }

    static hasImport(ast, element) {
        let found = false;
        ast.topLevelNodes.forEach((node) => {
            if (TreeUtil.isImport(node)) {
                if (node.getSource().replace(/\s/g, '') === element.getSource().replace(/\s/g, '')) {
                    found = true;
                }
            }
        });
        return found;
    }

    static mergeService(targetService, newService) {
        SwaggerUtil.mergeAnnotations(targetService, newService);

        // iterate resource.
        newService.resources.forEach((resource) => {
            // find matching resource
            const match = SwaggerUtil.matchResource(targetService, resource);
            if (match) {
                // merge resource annotations
                SwaggerUtil.mergeAnnotations(match, resource);
            } else {
                // if a matching resource is not found add it to service.
                resource.body.statements = [];
                targetService.addResources(resource);
            }
        });
    }


    static mergeAnnotations(to, from) {
        // iterate annotations
        from.annotationAttachments.forEach((node) => {
            const match = to.annotationAttachments.find((annotation) => {
                return node.annotationName.value === annotation.annotationName.value &&
                    node.packageAlias.value === annotation.packageAlias.value;
            });
            // if not found add annotations
            if (match) {
                if (TreeUtil.isRecordLiteralExpr(match.expression) &&
                    TreeUtil.isRecordLiteralExpr(node.expression)) {
                    node.expression.keyValuePairs.forEach((newRec) => {
                        const matchRec = match.expression.keyValuePairs.find((oldRec) => {
                            return oldRec.key.variableName.value === newRec.key.variableName.value;
                        });
                        if (matchRec) {
                            match.expression.replaceKeyValuePairs(matchRec, newRec);
                        } else {
                            match.expression.addKeyValuePairs(newRec);
                        }
                    });
                }
            } else {
                // if found add attributes.
                to.addAnnotationAttachments(node);
            }
        });
    }

    static matchResource(targetService, resource) {
        // iterate and find a matching name.
        return targetService.resources.find((node) => {
            return node.name.value === resource.name.value;
        });
    }

    static cleanResources(ast) {
        ast.topLevelNodes.forEach((element) => {
            // merge service.
            if (TreeUtil.isService(element)) {
                element.resources.forEach((resource) => {
                    resource.body.statements = [];
                });
            }
        });
        return ast;
    }
}

export default SwaggerUtil;
