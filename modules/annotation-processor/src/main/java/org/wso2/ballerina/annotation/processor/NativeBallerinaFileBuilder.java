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

package org.wso2.ballerina.annotation.processor;

import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaAnnotation;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaFunction;
import org.wso2.ballerina.core.nativeimpl.annotations.BallerinaTypeConvertor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Builder class to generate ballerina files for the native APIs.
 */
public class NativeBallerinaFileBuilder {

    private static final PrintStream ERROR = System.err;
    private Map<String, NativeBallerinaPackage> nativePackages;
    private String targetDirectory;

    public NativeBallerinaFileBuilder(String targetDir) {
        this.targetDirectory = targetDir;
        nativePackages = new HashMap<String, NativeBallerinaPackage>();
    }

    /**
     * Adds connectors to the {@link NativeBallerinaPackage}
     *
     * @param connectors connectors to be added
     */
    public void addConnectors(Collection<Connector> connectors) {
        nativePackages.forEach((k, v) -> {
            List<Connector> pkgsConnectors =
                    connectors.stream().filter(p -> k.equals(p.getBalConnector().packageName()))
                            .collect(Collectors.toList());
            v.setNativeConnectors(pkgsConnectors);
        });
    }

    /**
     * Create directories for native packages and write natives.bal files.
     */
    public void build() {
        nativePackages.forEach((k, v) -> {
            String directory = Arrays.asList(k.split("\\.")).stream().collect(Collectors.joining(File.separator));
            try {
                Files.createDirectories(Paths.get(targetDirectory, directory));
                try (BufferedWriter writer =
                        Files.newBufferedWriter(Paths.get(targetDirectory, directory, "natives.bal"))) {
                    writer.write(v.toString());
                }
            } catch (IOException e) {
                ERROR.println("Couldn't create native files for [Directory] " + directory + " cause: " + e);
            }
        });
    }

    /**
     * Add a native construct to the ballerina file builder.
     *
     * @param packageName name of the package which the construct is belong to
     * @param construct native construct to be added.
     */
    public void addNativeConstruct(String packageName, Object construct, BallerinaAnnotation[] annotations) {
        NativeBallerinaPackage nativeBallerinaPackage = nativePackages.get(packageName);
        if (nativeBallerinaPackage == null) {
            nativeBallerinaPackage = new NativeBallerinaPackage(packageName);
            nativePackages.put(packageName, nativeBallerinaPackage);
        }

        if (construct instanceof BallerinaFunction) {
            NativeBallerinaFunction func = new NativeBallerinaFunction((BallerinaFunction) construct);
            func.setAnnotations(annotations);
            nativeBallerinaPackage.addNativeFunction(func);
        } else if (construct instanceof BallerinaTypeConvertor) {
            NativeBallerinaTypeConverter converter =
                    new NativeBallerinaTypeConverter((BallerinaTypeConvertor) construct);
            nativeBallerinaPackage.addNativeTypeConverter(converter);
        }
    }

    /**
     * Holds content of a natives.bal file of a given package.
     */
    static class NativeBallerinaPackage {
        private String packageName;
        private List<NativeBallerinaFunction> nativeFunctions;
        private List<Connector> nativeConnectors;
        private List<NativeBallerinaTypeConverter> nativeBallerinaTypeConverters;

        public NativeBallerinaPackage(String pkgName) {
            this.setPackageName(pkgName);
            nativeFunctions = new ArrayList<NativeBallerinaFunction>();
            nativeConnectors = new ArrayList<Connector>();
            nativeBallerinaTypeConverters = new ArrayList<NativeBallerinaTypeConverter>();
        }

        public void addNativeFunction(NativeBallerinaFunction func) {
            nativeFunctions.add(func);
        }

        public void addNativeTypeConverter(NativeBallerinaTypeConverter converter) {
            nativeBallerinaTypeConverters.add(converter);
        }

        public String getPackageName() {
            return packageName;
        }

        public void setPackageName(String packageName) {
            this.packageName = packageName;
        }

        public void setNativeConnectors(List<Connector> nativeConnectors) {
            this.nativeConnectors = nativeConnectors;
        }

        @Override
        public String toString() {
            return "package " + packageName + ";\n\n"
                    + nativeFunctions.stream().map(k -> k.toString()).collect(Collectors.joining("\n\n"))
                    + (nativeFunctions.size() > 0 ? "\n\n" : "")
                    + nativeBallerinaTypeConverters.stream().map(k -> k.toString()).collect(Collectors.joining("\n\n"))
                    + (nativeBallerinaTypeConverters.size() > 0 ? "\n\n" : "")
                    + nativeConnectors.stream().map(k -> k.toString()).collect(Collectors.joining("\n\n"));
        }

    }

    /**
     * Holds a native ballerina function.
     */
    static class NativeBallerinaFunction {
        private BallerinaFunction balFunc;
        private List<Annotation> annotations;

        public NativeBallerinaFunction(BallerinaFunction func) {
            this.balFunc = func;
            this.annotations = new ArrayList<>();
        }

        public void setAnnotations(BallerinaAnnotation[] annotations) {
            this.annotations = Utils.getAnnotations(annotations);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            Utils.appendAnnotationStrings(sb, annotations);
            sb.append(annotations.size() > 0 ? "\n" : "");
            sb.append("native function ").append(balFunc.functionName());
            Utils.getInputParams(balFunc.args(), sb);
            Utils.getReturnParams(balFunc.returnType(), sb);
            sb.append(";");
            return sb.toString();
        }
    }

    /**
     * Holds a native ballerina type converter.
     */
    static class NativeBallerinaTypeConverter {
        private BallerinaTypeConvertor balTypeConverter;
        private List<Annotation> annotations;

        public NativeBallerinaTypeConverter(BallerinaTypeConvertor converter) {
            this.balTypeConverter = converter;
            this.annotations = new ArrayList<>();
        }

        public void setAnnotations(BallerinaAnnotation[] annotations) {
            this.annotations = Utils.getAnnotations(annotations);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            Utils.appendAnnotationStrings(sb, annotations);
            sb.append("native typeconvertor ").append(balTypeConverter.typeConverterName());
            Utils.getInputParams(balTypeConverter.args(), sb);
            Utils.getReturnParams(balTypeConverter.returnType(), sb);
            sb.append(";");
            return sb.toString();
        }
    }

}
