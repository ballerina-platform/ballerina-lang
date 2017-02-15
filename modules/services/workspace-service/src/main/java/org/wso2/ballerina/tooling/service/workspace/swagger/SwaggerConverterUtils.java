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

package org.wso2.ballerina.tooling.service.workspace.swagger;

import io.swagger.codegen.ClientOptInput;
import io.swagger.codegen.ClientOpts;
import io.swagger.codegen.CodegenConfig;
import io.swagger.codegen.CodegenOperation;
import io.swagger.codegen.DefaultGenerator;
import io.swagger.models.HttpMethod;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.parser.Swagger20Parser;
import io.swagger.util.Json;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.wso2.ballerina.core.model.GlobalScope;
import org.wso2.ballerina.core.model.NodeLocation;
import org.wso2.ballerina.core.model.Resource;
import org.wso2.ballerina.core.model.Annotation;
import org.wso2.ballerina.core.model.Service;
import org.wso2.ballerina.core.model.BLangPackage;
import org.wso2.ballerina.core.model.SymbolName;
import org.wso2.ballerina.core.model.SymbolScope;
import org.wso2.ballerina.core.model.builder.BLangModelBuilder;
import org.wso2.ballerina.core.model.BallerinaFile;
import org.wso2.ballerina.core.model.ParameterDef;
import org.wso2.ballerina.core.model.statements.BlockStmt;
import org.wso2.ballerina.core.model.types.BTypes;
import org.wso2.ballerina.core.model.types.SimpleTypeName;
import org.wso2.ballerina.core.model.values.BMessage;
import org.wso2.ballerina.core.model.Worker;
import org.wso2.ballerina.core.parser.BallerinaLexer;
import org.wso2.ballerina.core.parser.BallerinaParser;
import org.wso2.ballerina.core.parser.BallerinaParserErrorStrategy;
import org.wso2.ballerina.core.parser.antlr4.BLangAntlr4Listener;
import org.wso2.ballerina.core.runtime.dispatching.Constants;
import org.wso2.ballerina.tooling.service.workspace.swagger.generators.BallerinaCodeGenerator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Swagger related utility classes.
 */

public class SwaggerConverterUtils {

    /**
     * Maximum loop count when creating temp directories.
     */
    private static final int TEMP_DIR_ATTEMPTS = 10000;

    /**
     * This method will extract service definitions from ballerina source
     *
     * @param ballerinaDefinition @String service definition to be process as ballerina
     * @return @List<Service> which contain all services within give ballerina source
     * @throws IOException when input stream handling error.
     */
    public static Service[] getServicesFromBallerinaDefinition(String ballerinaDefinition) throws IOException {
        BallerinaFile bFile = getBFileFromBallerinaDefinition(ballerinaDefinition);
        return bFile.getServices();
    }

    /**
     * Generate ballerina fine from the String definition
     * @param ballerinaDefinition
     * @return
     * @throws IOException
     */
    public static BallerinaFile getBFileFromBallerinaDefinition(String ballerinaDefinition) throws IOException {
        //TODO this method need to replaced with the utility provided by ballerina core.
        ANTLRInputStream antlrInputStream = new ANTLRInputStream(ballerinaDefinition);
        BallerinaLexer ballerinaLexer = new BallerinaLexer(antlrInputStream);
        CommonTokenStream ballerinaToken = new CommonTokenStream(ballerinaLexer);
        BallerinaParser ballerinaParser = new BallerinaParser(ballerinaToken);
        ballerinaParser.setErrorHandler(new BallerinaParserErrorStrategy());
        GlobalScope globalScope = GlobalScope.getInstance();
        BTypes.loadBuiltInTypes(globalScope);
        BLangPackage bLangPackage = new BLangPackage(globalScope);
        BLangModelBuilder bLangModelBuilder = new BLangModelBuilder(bLangPackage);
        BLangAntlr4Listener ballerinaBaseListener = new BLangAntlr4Listener(bLangModelBuilder);
        ballerinaParser.addParseListener(ballerinaBaseListener);
        ballerinaParser.compilationUnit();
        BallerinaFile bFile = bLangModelBuilder.build();
        return bFile;
    }

    /**
     * This method will generate Ballerina service from Swagger definition.
     *
     * @param swaggerDefinition @String swagger definition
     * @return @Service
     * @throws IOException
     */
    public static Service getServiceFromSwaggerDefinition(String swaggerDefinition) throws IOException {
        //TODO this logic need to be reviewed and fix issues. This is temporary commit to test swagger UI flow
        Swagger20Parser swagger20Parser = new Swagger20Parser();
        Swagger swagger = swagger20Parser.parse(swaggerDefinition);
        //Iterate through service annotations and add them to service
        Service.ServiceBuilder serviceBuilder = new Service.ServiceBuilder(
                new BLangPackage(GlobalScope.getInstance()));
        serviceBuilder.setName(swagger.getBasePath());
        Service service = serviceBuilder.buildService();
        CodegenConfig codegenConfig = new BallerinaCodeGenerator();
        codegenConfig.setOutputDir(createTempDir().getAbsolutePath());
        ClientOptInput clientOptInput = new ClientOptInput().opts(new ClientOpts()).swagger(swagger).
                config(codegenConfig);
        DefaultGenerator generator = new DefaultGenerator();
        generator.opts(clientOptInput);
        Map<String, List<CodegenOperation>> paths = generator.processPaths(swagger.getPaths());
        Resource[] resources1 = null;
        for (String path : paths.keySet()) {
            List<CodegenOperation> ops = paths.get(path);
            resources1 = mapSwaggerPathsToResources(ops);
        }
        List<Annotation> serviceAnnotationArrayList = new ArrayList<Annotation>();
        serviceAnnotationArrayList.add(new Annotation(null, new SymbolName(Constants.BASE_PATH),
                swagger.getBasePath(), null));
        serviceAnnotationArrayList.add(new Annotation(null, new SymbolName(Constants.HOST),
                swagger.getHost(), null));
        serviceAnnotationArrayList.add(new Annotation(null, new SymbolName("Info"),
                Json.pretty(swagger.getInfo()).toString(), null));
        service.setAnnotations(serviceAnnotationArrayList.toArray(
                new Annotation[serviceAnnotationArrayList.size()]));
        //Iterate through paths and add them as resources
        service.setResources(resources1);
        return service;
    }

    /**
     * Atomically creates a new directory somewhere beneath the system's temporary directory (as defined by the {@code
     * java.io.tmpdir} system property), and returns its name.
     *
     * @return the newly-created directory
     * @throws IllegalStateException if the directory could not be created
     */
    private static File createTempDir() {
        File baseDir = new File(System.getProperty("java.io.tmpdir"));
        String baseName = System.currentTimeMillis() + "-";

        for (int counter = 0; counter < TEMP_DIR_ATTEMPTS; counter++) {
            File tempDir = new File(baseDir, baseName + counter);
            if (tempDir.mkdir()) {
                return tempDir;
            }
        }
        throw new IllegalStateException(
                "Failed to create directory within "
                        + TEMP_DIR_ATTEMPTS
                        + " attempts (tried "
                        + baseName
                        + "0 to "
                        + baseName
                        + (TEMP_DIR_ATTEMPTS - 1)
                        + ')');
    }


    /**
     * This method will convert swagger path List into ballerina @Resource array
     *
     * @param pathMap Swagger @CodegenOperation list to be processed
     * @return @Resource array generated from pathMap
     */
    public static Resource[] mapSwaggerPathsToResources(List<CodegenOperation> pathMap) {
        //TODO this logic need to be reviewed and fix issues. This is temporary commit to test swagger UI flow

        List<Resource> resourceList = new ArrayList<Resource>();
        for (CodegenOperation entry : pathMap) {
            String httpMethod = entry.httpMethod;
            String operationId = entry.operationId;
            Resource.ResourceBuilder resourceBuilder = new Resource.ResourceBuilder(
                    new BLangPackage(GlobalScope.getInstance()));
            resourceBuilder.setName(operationId);
            if (entry.hasConsumes) {
                resourceBuilder.addAnnotation(
                        new Annotation(null, new SymbolName("Consumes"), entry.consumes.toString(), null));
            }
            if (entry.hasProduces) {
                resourceBuilder.addAnnotation(
                        new Annotation(null, new SymbolName("Produces"), entry.produces.toString(), null));
            }
            if (entry.summary != null) {
                resourceBuilder.addAnnotation(
                        new Annotation(null, new SymbolName("Summary"), entry.summary.toString(), null));
            }
            if (entry.notes != null) {
                resourceBuilder.addAnnotation(
                        new Annotation(null, new SymbolName("Description"), entry.notes.toString(), null));
            }
            if (entry.path != null && entry.path.length() > 0) {
                resourceBuilder
                        .addAnnotation(new Annotation(null, new SymbolName("Path"), entry.path.toString(), null));
            }
            if (entry.httpMethod != null && entry.httpMethod.length() > 0) {
                resourceBuilder.addAnnotation(new Annotation(null, new SymbolName(httpMethod), "", null));
            }
            //This resource initiation was required because resource do have both
            //annotation map and array. But there is no way to update array other than
            //constructor method.
            resourceBuilder.setName(entry.nickname);
            //Following code block will generate message input parameter definition for newly created
            //resource as -->	resource TestPost(message m) {
            //This logic can be improved to pass user defined types.
            ParameterDef parameterDef = new ParameterDef(
                    new NodeLocation("<unknown>"),"m", new SimpleTypeName("message"), new SymbolName("m"),
                    resourceBuilder.buildResource());
            //Then add created parameter.
            resourceBuilder.addParameter(parameterDef);
            Resource resourceToBeAdd = resourceBuilder.buildResource();
            resourceList.add(resourceToBeAdd);
        }
        return resourceList.toArray(new Resource[resourceList.size()]);
    }

    /**
     *
     * @param pathMap
     * @return
     */
    public static Resource[] mapPathsToResources(Map<String, Path> pathMap) {
        //TODO this logic need to be reviewed and fix issues. This is temporary commit to test swagger UI flow
        List<Resource> resourceList = new ArrayList<Resource>();
        for (Map.Entry<String, Path> entry : pathMap.entrySet()) {
            Path path = entry.getValue();
            Resource.ResourceBuilder resourceBuilder = new Resource.ResourceBuilder(
                    new BLangPackage(GlobalScope.getInstance()));
            for (Map.Entry<HttpMethod, Operation> operationEntry : path.getOperationMap().entrySet()) {
                resourceBuilder.addAnnotation(
                        new Annotation(null, new SymbolName(operationEntry.getKey().toString()), null, null));

                resourceBuilder.setSymbolName(new SymbolName(operationEntry.getKey().name()));
            }
            Resource resource = resourceBuilder.buildResource();
            resourceList.add(resource);
        }
        pathMap.forEach((pathString, pathObject) -> {

        });
        return resourceList.toArray(new Resource[resourceList.size()]);
    }

    /**
     * This method will merge swagger definition based to ballerina service.
     *
     * @param ballerinaService
     * @param swaggerService
     * @return
     */
    public static Service mergeBallerinaService(Service ballerinaService, Service swaggerService) {
        //TODO this logic need to be reviewed and fix issues. This is temporary commit to test swagger UI flow
        //Secondary service annotations are coming from swagger. So we need to merge and update.
        ballerinaService.setAnnotations(
                mergeAnnotations(ballerinaService.getAnnotations(), swaggerService.getAnnotations()));
        List<Resource> resourceList = new ArrayList<Resource>();
        for (Resource resource : swaggerService.getResources()) {
            boolean isExistingResource = false;
            for (Resource originalResource : ballerinaService.getResources()) {
                if (isResourceMatch(resource, originalResource)) {
                    isExistingResource = true;
                    //Here is a resource math. Do assignments
                    //merge annotations
                    Resource.ResourceBuilder resourceBuilder =
                            new Resource.ResourceBuilder(originalResource.getEnclosingScope());
                    resourceBuilder.setName(originalResource.getName());
                    resourceBuilder.setPkgPath(originalResource.getPackagePath());
                    resourceBuilder.setBody(originalResource.getResourceBody());
                    resourceBuilder.setNodeLocation(originalResource.getNodeLocation());
                    for (Annotation annotation :
                            mergeAnnotations(originalResource.getAnnotations(), resource.getAnnotations())) {
                        resourceBuilder.addAnnotation(annotation);
                    }
                    for (Worker worker : originalResource.getWorkers()) {
                        resourceBuilder.addWorker(worker);
                    }
                    for (ParameterDef parameterDef : originalResource.getParameterDefs()) {
                        resourceBuilder.addParameter(parameterDef);
                    }
                    for (ParameterDef parameterDef : originalResource.getReturnParameters()) {
                        resourceBuilder.addReturnParameter(parameterDef);
                    }
                    resourceList.add(resourceBuilder.buildResource());
                }
            }
            if (!isExistingResource) {
                resourceList.add(resource);
                //This is completely new resource
            }
        }
        ballerinaService.setResources(mergeResources(resourceList, ballerinaService.getResources()));
        return ballerinaService;
    }

    /**
     * This method will merge annotations from two different services or resources.
     * @param annotations
     * @param annotationsToMerge
     * @return
     */
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

    /**
     * Clone annotations array
     * @param annotations
     * @return
     */
    private static Annotation[] clone(Annotation[] annotations) {
        return annotations == null ? null : (Annotation[]) annotations.clone();
    }

    /**
     * Check both resources are represent same resource path and http verb.
     * Within a service resource should have unique resource path and HTTP verb combination.
     *
     * @param swaggerResource
     * @param ballerinaResource
     * @return
     */
    public static boolean isResourceMatch(Resource swaggerResource, Resource ballerinaResource) {
        String path = "Root";
        String verb = "";
        for (Annotation annotation : ballerinaResource.getAnnotations()) {
            if (annotation.getName().equalsIgnoreCase("Path")) {
                path = path.startsWith("/") && path.length() > 1 ? path.substring(1) : path;
            } else if (annotation.getName().matches(SwaggerResourceMapper.HTTP_VERB_MATCHING_PATTERN)) {
                verb = annotation.getName();
            }
        }
        String resourceKey = path + verb;
        return swaggerResource.getSymbolName().getName().equalsIgnoreCase(resourceKey);
    }

    /**
     * Remove duplicate resources and merge them.
     *
     * @param resourceList
     * @param resources
     * @return
     */
    public static Resource[] mergeResources(List<Resource> resourceList, Resource[] resources) {
        for (int i = 0; i < resources.length; i++) {
            Resource resource = resources[i];
            boolean isMatched = false;
            for (Resource resourceFromList : resourceList) {
                if (resourceFromList.getSymbolName().getName().equalsIgnoreCase(
                        resource.getSymbolName().getName())) {
                    isMatched = true;
                    //match means its there in list
                }
            }
            if (!isMatched) {
                //If this is complete new resource then add it to another list.
                resourceList.add(resource);
            }

        }
        return resourceList.toArray(new Resource[resourceList.size()]);
    }
}
