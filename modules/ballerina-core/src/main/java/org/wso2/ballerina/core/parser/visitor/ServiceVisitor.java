/*
 * Copyright (c) 2016, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.parser.visitor;

import org.wso2.ballerina.core.model.Identifier;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.parser.BallerinaBaseVisitor;
import org.wso2.ballerina.core.parser.BallerinaParser;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor for service
 */
public class ServiceVisitor extends BallerinaBaseVisitor {

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitServiceDefinition(BallerinaParser.ServiceDefinitionContext ctx) {
        // Create the service object
        String serviceName = ctx.Identifier().getText();
        Identifier identifier = new Identifier(serviceName);
        Service serviceObject = new Service(identifier);

        // Read the Annotations
        // Skipped for the moment

        // Create the service body and resources
        List<Resource> resources = (List<Resource>) this.
                visitServiceBodyDeclaration(ctx.serviceBody().serviceBodyDeclaration());
        serviceObject.setResources(resources);

        return serviceObject;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitServiceBody(BallerinaParser.ServiceBodyContext ctx) {
        return super.visitServiceBody(ctx);
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     *
     * @param ctx
     */
    @Override
    public Object visitServiceBodyDeclaration(BallerinaParser.ServiceBodyDeclarationContext ctx) {
        // Read the resources
        List<Resource> resources = new ArrayList<>();
        ResourceVisitor resourceVisitor = new ResourceVisitor();
        for (BallerinaParser.ResourceDefinitionContext rdc : ctx.resourceDefinition()) {
            Resource resourceObject = (Resource) rdc.accept(resourceVisitor);
            resources.add(resourceObject);
            //serviceObject.addResource(resourceObject);
        }
        return resources;
    }
}
