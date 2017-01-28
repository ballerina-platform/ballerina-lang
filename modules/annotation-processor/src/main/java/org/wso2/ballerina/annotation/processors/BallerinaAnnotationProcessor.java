/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.annotation.processors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;

import java.io.PrintStream;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;

/**
 * Configuration annotation processor extending AbstractProcessor.
 * Reads all classes annotated Configuration
 *
 * @since 5.2.0
 */
@SupportedAnnotationTypes({"org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction",
                           "org.wso2.ballerina.core.nativeimpl.annotations.BallerinaConnector"})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BallerinaAnnotationProcessor extends AbstractProcessor {

    private static final Logger log = LoggerFactory.getLogger(BallerinaAnnotationProcessor.class);
    PrintStream out =  System.out;

    public BallerinaAnnotationProcessor() {
        super();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        out.println("\n =============== Native Function & Connectors ====================");
        visitNativeFunctions(roundEnv);
        visitNativeConnectors(roundEnv);
        return true;
    }
    
    private void visitNativeFunctions(RoundEnvironment roundEnv) {
        Set<Element> configSet = (Set<Element>) roundEnv.getElementsAnnotatedWith(BallerinaFunction.class);
        for (Element element : configSet) {
            BallerinaFunction balFunction = element.getAnnotation(BallerinaFunction.class);
            out.println(balFunction.packageName() + ":" + balFunction.functionName());
        }
    }
    
    private void visitNativeConnectors(RoundEnvironment roundEnv) {
        Set<Element> configSet = (Set<Element>) roundEnv.getElementsAnnotatedWith(BallerinaConnector.class);
        for (Element element : configSet) {
            BallerinaConnector balConnector = element.getAnnotation(BallerinaConnector.class);
            out.println(balConnector.packageName() + ":" + balConnector.connectorName());
        }
    }
}
