/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.composer.service.workspace.swagger;

import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Response;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import org.ballerinalang.model.Annotation;
import org.ballerinalang.model.ParameterDef;
import org.ballerinalang.model.Resource;
import org.ballerinalang.services.dispatchers.http.Constants;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class will do resource mapping from ballerina to swagger.
 */
public class SwaggerResourceMapper {

    private Resource resource;
    private Operation operation;


    /**
     * Get Swagger operation object associated with current resource
     *
     * @return Swagger operation object associated with current resource
     */
    public Operation getOperation() {
        return operation;
    }


    /**
     * Set Swagger operation object associated with current resource
     *
     * @param operation Swagger operation object associated with current resource
     */
    public void setOperation(Operation operation) {
        this.operation = operation;
    }


    /**
     * Get Ballerina Resource object associated with current resource
     *
     * @return Ballerina Resource object associated with current resource
     */
    public Resource getResource() {
        return resource;
    }


    /**
     * Set Ballerina Resource object associated with current resource
     *
     * @param resource Ballerina Resource object associated with current resource
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }


    /**
     * This method will convert ballerina resource to swagger path objects.
     *
     * @param resources Resource array to be convert.
     * @return map of string and swagger path objects.
     */
    protected Map<String, Path> convertResourcesToOperations(Resource[] resources) {
        Map<String, Path> map = new ConcurrentHashMap<>();
        for (Resource subResource : resources) {
            OperationAdaptor operationAdaptor = convertResourceToOperation(subResource);
            Path path = map.get(operationAdaptor.getPath());
            //TODO this check need to be improve to avoid repetition checks and http head support need to add.
            if (path == null) {
                path = new Path();
                map.put(operationAdaptor.getPath(), path);
            }
            String httpOperation = operationAdaptor.getHttpOperation();
            Operation operation = operationAdaptor.getOperation();
            switch (httpOperation) {
                case SwaggerBallerinaConstants.HTTP_PACKAGE_PREFIX + Constants.ANNOTATION_METHOD_GET:
                    path.get(operation);
                    break;
                case SwaggerBallerinaConstants.HTTP_PACKAGE_PREFIX + Constants.ANNOTATION_METHOD_PUT:
                    path.put(operation);
                    break;
                case SwaggerBallerinaConstants.HTTP_PACKAGE_PREFIX + Constants.ANNOTATION_METHOD_POST:
                    path.post(operation);
                    break;
                case SwaggerBallerinaConstants.HTTP_PACKAGE_PREFIX + Constants.ANNOTATION_METHOD_DELETE:
                    path.delete(operation);
                    break;
                case SwaggerBallerinaConstants.HTTP_PACKAGE_PREFIX + Constants.ANNOTATION_METHOD_OPTIONS:
                    path.options(operation);
                    break;
                case SwaggerBallerinaConstants.HTTP_PACKAGE_PREFIX + Constants.ANNOTATION_METHOD_PATCH:
                    path.patch(operation);
                    break;
                case "http:HEAD":
                    path.head(operation);
                    break;
                default:
                    break;
            }
        }
        return map;
    }


    /**
     * This method will convert ballerina @Resource to ballerina @OperationAdaptor
     *
     * @param resource Resource array to be convert.
     * @return @OperationAdaptor of string and swagger path objects.
     */
    private OperationAdaptor convertResourceToOperation(Resource resource) {
        OperationAdaptor op = new OperationAdaptor();
        if (resource != null) {
            Annotation[] resourceAnnotations = resource.getResourceAnnotations();
            //Adding default response
            //TODO need to implement nested response support and then use response annotation.
            Response response = new Response()
                    .description("Successful")
                    .example("application/json", "Ok");
            op.getOperation().response(200, response);
            //Default path should be /
            String path = "/";
            op.setPath(path);
            for (ParameterDef parameterDef : resource.getParameterDefs()) {
                String typeName = parameterDef.getTypeName().getName();
                if (!typeName.equalsIgnoreCase("message") && parameterDef.getAnnotations() != null) {
                    //Add query parameter
                    if (parameterDef.getAnnotations().get(0).getName().equalsIgnoreCase("http:QueryParam")) {
                        QueryParameter queryParameter = new QueryParameter();
                        queryParameter.setType(typeName);
                        queryParameter.setIn("query");
                        queryParameter.setVendorExtension(SwaggerBallerinaConstants.VARIABLE_UUID_NAME, parameterDef
                                .getName());
                        String parameterName = parameterDef.getAnnotations().get(0).getValue();
                        if ((parameterName == null) || parameterName.isEmpty()) {
                            parameterName = parameterDef.getName();
                        }
                        queryParameter.setName(parameterName);
                        queryParameter.required(true);
                        op.getOperation().addParameter(queryParameter);
                    }
                    if (parameterDef.getAnnotations().get(0).getName().equalsIgnoreCase("http:PathParam")) {
                        PathParameter pathParameter = new PathParameter();
                        pathParameter.setType(typeName);
                        String parameterName = parameterDef.getAnnotations().get(0).getValue();
                        if ((parameterName == null) || parameterName.isEmpty()) {
                            parameterName = parameterDef.getName();
                        }
                        pathParameter.setName(parameterName);
                        pathParameter.setIn("path");
                        pathParameter.setVendorExtension(SwaggerBallerinaConstants.VARIABLE_UUID_NAME, parameterDef
                                .getName());
                        pathParameter.required(true);
                        op.getOperation().addParameter(pathParameter);
                    }
                }
            }
            if (resourceAnnotations != null) {
                //TODO add all supported annotation mapping after annotation model finalized.
                for (Annotation annotation : resourceAnnotations) {
                    if (annotation.getName().equalsIgnoreCase("http:Consumes")) {
                        op.getOperation().consumes(annotation.getValue());
                    } else if (annotation.getName().equalsIgnoreCase("http:Produces")) {
                        op.getOperation().produces(annotation.getValue());
                    } else if (annotation.getName().equalsIgnoreCase("http:Path")) {
                        op.setPath(annotation.getValue());
                    } else if (annotation.getName().equalsIgnoreCase("http:Summary")) {
                        op.getOperation().setSummary(annotation.getValue());
                    } else if (annotation.getName().equalsIgnoreCase("http:Description")) {
                        op.getOperation().setDescription(annotation.getValue());
                    } else if (annotation.getName().matches(SwaggerBallerinaConstants.
                            HTTP_VERB_MATCHING_PATTERN)) {
                        op.setHttpOperation(annotation.getName());
                    }
                /*
                Other annotations do not support by swagger.
                //TODO process them and use if we can map to generic attributes.
                else {
                    if(annotation.getName()!=null && annotation.getValue()!= null){
                    }
                }*/
                }
                op.getOperation().setVendorExtension(SwaggerBallerinaConstants.RESOURCE_UUID_NAME,
                        SwaggerConverterUtils.generateServiceUUID(op.getPath(), op.getHttpOperation()));
            }
        }
        return op;

    }

}
