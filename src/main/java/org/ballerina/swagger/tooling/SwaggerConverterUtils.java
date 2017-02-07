/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerina.swagger.tooling;

import io.swagger.codegen.*;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.parser.Swagger20Parser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.wso2.ballerina.core.model.*;
import org.wso2.ballerina.core.model.builder.BLangModelBuilder;
import org.wso2.ballerina.core.parser.BallerinaLexer;
import org.wso2.ballerina.core.parser.BallerinaParser;
import org.wso2.ballerina.core.parser.antlr4.BLangAntlr4Listener;
import org.ballerina.swagger.tooling.generators.BallerinaCodeGenerator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Swagger related utility classes.
 */

public class SwaggerConverterUtils {

    /**
     * This method will extract service definitions from ballerina source
     *
     * @param ballerinaDefinition @String service definition to be process as ballerina
     * @return @List<Service> which contain all services within give ballerina source
     * @throws IOException when input stream handling error.
     */
    public static List<Service> getServicesFromBallerinaDefinition(String ballerinaDefinition) throws IOException {
        BallerinaFile bFile = getBFileFromBallerinaDefinition(ballerinaDefinition);
        List<Service> services = bFile.getServices();
        return services;
    }

    public static BallerinaFile getBFileFromBallerinaDefinition(String ballerinaDefinition) throws IOException {
        InputStream stream = new ByteArrayInputStream(ballerinaDefinition.
                getBytes(StandardCharsets.UTF_8));
        ANTLRInputStream antlrInputStream = new ANTLRInputStream(stream);
        BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
        CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);
        BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
        BLangModelBuilder modelBuilder = new BLangModelBuilder();
        BLangAntlr4Listener langModelBuilder = new BLangAntlr4Listener(modelBuilder);
        ballerinaParser.addParseListener(langModelBuilder);
        ballerinaParser.compilationUnit();
        BallerinaFile bFile = modelBuilder.build();
        return bFile;
    }

    public static Service getServiceFromSwaggerDefinition(String swaggerDefinition) throws IOException {
        //TODO this logic need to be reviewed and fix issues. This is temporary commit to test swagger UI flow
        Swagger20Parser swagger20Parser = new Swagger20Parser();
        Swagger swagger = swagger20Parser.parse(swaggerDefinition);
        //Iterate through service annotations and add them to service
        Service service = new Service(new SymbolName(swagger.getBasePath()));
        CodegenConfig codegenConfig = new BallerinaCodeGenerator();
        codegenConfig.setOutputDir("/home/sanjeewa/work/dev-code/swagger2BallerinaConnector/test");
        ClientOptInput clientOptInput = new ClientOptInput().opts(new ClientOpts()).swagger(swagger).
                config(codegenConfig);
        DefaultGenerator generator = new DefaultGenerator();
        generator.opts(clientOptInput);
        Map<String, List<CodegenOperation>> paths = generator.processPaths(swagger.getPaths());
        Set<String> opIds = new HashSet<String>();
        Resource[] resources1 = null;
        for (String path : paths.keySet()) {
            List<CodegenOperation> ops = paths.get(path);
            resources1 = mapSwaggerPathsToResources(ops);
            /*for (CodegenOperation op : ops) {
                opIds.add(op.operationId);
                opIds.add(op.consumes.toString();

            }*/
        }
        generator.generate();
        List<Annotation> serviceAnnotationArrayList = new ArrayList<Annotation>();
        serviceAnnotationArrayList.add(new Annotation("BasePath", swagger.getBasePath()));
        serviceAnnotationArrayList.add(new Annotation("Host", swagger.getHost()));
        service.setAnnotations(serviceAnnotationArrayList.toArray(new Annotation[serviceAnnotationArrayList.size()]));
        //Iterate through paths and add them as resources
        Resource[] resources = mapPathsToResources(swagger.getPaths());
        service.setResources(resources1);
        return service;
    }

    public static Resource[] mapSwaggerPathsToResources(List<CodegenOperation> pathMap) {
        //TODO this logic need to be reviewed and fix issues. This is temporary commit to test swagger UI flow
        Resource[] resources;
        Resource resource = new Resource();
        List<Resource> resourceList = new ArrayList<Resource>();
        for (CodegenOperation entry : pathMap) {
            String pathString = entry.path;
            Map<String, Annotation> annotationMap = new ConcurrentHashMap();
            String httpMethod = entry.httpMethod;
            String operationId = entry.operationId;
            resource.setSymbolName(new SymbolName(operationId));
            if (entry.hasConsumes) {
                annotationMap.put("Consumes", new Annotation("Consumes", entry.consumes.toString()));
            }
            if (entry.hasProduces) {
                annotationMap.put("Produces", new Annotation("Produces", entry.produces.toString()));
            }
            annotationMap.put(httpMethod, new Annotation(httpMethod, ""));
            resource.addAnnotation(new Annotation(new SymbolName(httpMethod), null, null));
            resource.setAnnotations(annotationMap);
            annotationMap.values().toArray(new Annotation[annotationMap.size()]);
            resourceList.add(resource);
        }
        return resourceList.toArray(new Resource[resourceList.size()]);
    }

    public static Resource[] mapPathsToResources(Map<String, Path> pathMap) {
        //TODO this logic need to be reviewed and fix issues. This is temporary commit to test swagger UI flow
        Resource[] resources;
        List<Resource> resourceList = new ArrayList<Resource>();
        for (Map.Entry<String, Path> entry : pathMap.entrySet()) {
            String pathString = entry.getKey();
            Path path = entry.getValue();
            Resource resource = new Resource();
            Map<String, Annotation> annotationMap = new ConcurrentHashMap();
            String httpMethodString;
            for (Map.Entry<HttpMethod, Operation> operationEntry : path.getOperationMap().entrySet()) {
                Operation currentOperation = operationEntry.getValue();
                //annotationMap.put("Consumes", new Annotation("Consumes", currentOperation.getConsumes().get(0)));
                //annotationMap.put("Produces", new Annotation("Produces", currentOperation.getProduces().get(0)));
                // annotationMap.put("Description", new Annotation("Description", currentOperation.getDescription()));
                annotationMap.put(operationEntry.getKey().toString(),
                        new Annotation(operationEntry.getKey().toString()));
                resource.setSymbolName(new SymbolName(operationEntry.getKey().name()));
                //operation1.getExternalDocs();
                //operation1.getOperationId();
                //operation1.getResponses();
                //operation1.getSummary();
            }
            resource.setAnnotations(annotationMap);
            resourceList.add(resource);
        }
        pathMap.forEach((pathString, pathObject) -> {

        });
        return resourceList.toArray(new Resource[resourceList.size()]);
    }

    public static Service mergeBallerinaService(Service originalService, Service secondaryService) {
        //TODO this logic need to be reviewed and fix issues. This is temporary commit to test swagger UI flow
        //Secondary service annotations are coming from swagger. So we need to merge and update.
        originalService.setAnnotations(mergeAnnotations(originalService.getAnnotations(), secondaryService.getAnnotations()));
        List<Resource> resourceList = new ArrayList<Resource>();
        for (Resource resource : secondaryService.getResources()) {
            boolean isExistingResource = false;
            for (Resource originalResource : originalService.getResources()) {
                if (originalResource.getSymbolName().getName().equalsIgnoreCase(
                        resource.getSymbolName().getName())) {
                    isExistingResource = true;
                    //Here is a resource math. Do assignments
                    //merge annotations
                    originalResource.setAnnotations(mergeAnnotationsAsMap(originalResource.getAnnotations(),
                            resource.getAnnotations()));
                }
            }
            if (!isExistingResource) {
                resourceList.add(resource);
                //This is completely new resource
            }
        }
        Collections.addAll(resourceList, originalService.getResources());
        originalService.setResources(resourceList.toArray(new Resource[resourceList.size()]));
        originalService.getSymbolName();
        originalService.getVariableDcls();
        originalService.getResources();
        originalService.getConnectorDcls();
        originalService.getServiceLocation();
        return originalService;
    }

    static Annotation[] mergeAnnotationsArray(Annotation[] a, Annotation[] b) {
        Set<Annotation> set = new HashSet<>(Arrays.asList(a));
        set.addAll(Arrays.asList(b));
        return set.toArray(new Annotation[0]);
    }

    public static Annotation[] mergeAnnotations(Annotation[] annotations, Annotation[] annotationsToMerge) {
        //TODO this logic need to be reviewed and fix issues. This is temporary commit to test swagger UI flow
        if (annotations == null) {
            return clone(annotationsToMerge);
        } else if (annotationsToMerge == null) {
            return clone(annotations);
        } else {
            //update annotations
            Map<String, Annotation> annotationMap = new ConcurrentHashMap<>();
            for (Annotation originalAnnotation : annotations) {
                //Add original annotations
                annotationMap.put(originalAnnotation.getName(), originalAnnotation);
            }
            for (Annotation annotationToMerge : annotationsToMerge) {
                //merge annotations
                annotationMap.put(annotationToMerge.getName(), annotationToMerge);
            }
            return annotationMap.values().toArray(new Annotation[annotationMap.size()]);
        }
    }

    public static Map<String, Annotation> mergeAnnotationsAsMap(Annotation[] annotations,
                                                                Annotation[] annotationsToMerge) {
        //update annotations
        //TODO this logic need to be reviewed and fix issues. This is temporary commit to test swagger UI flow
        Map<String, Annotation> annotationMap = new ConcurrentHashMap<>();
        for (Annotation originalAnnotation : annotations) {
            //Add original annotations
            annotationMap.put(originalAnnotation.getName(), originalAnnotation);
        }
        for (Annotation annotationToMerge : annotationsToMerge) {
            //merge annotations
            annotationMap.put(annotationToMerge.getName(), annotationToMerge);
        }
        return annotationMap;
    }

    private static Annotation[] clone(Annotation[] annotations) {
        return annotations == null ? null : (Annotation[]) annotations.clone();
    }
}
