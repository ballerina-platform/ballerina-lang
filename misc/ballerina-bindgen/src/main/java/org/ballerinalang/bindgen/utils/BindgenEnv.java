/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.bindgen.utils;

import io.ballerina.projects.Package;
import io.ballerina.projects.Project;
import io.ballerina.projects.TomlDocument;
import org.ballerinalang.bindgen.model.JError;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.ballerinalang.bindgen.utils.BindgenUtils.isPublicClass;

/**
 * The model that stores the details which will be passed on and updated while generating the mappings.
 *
 * @since 2.0.0
 */
public class BindgenEnv {

    private boolean modulesFlag = false; // Stores if the bindings are mapped as Java package per Ballerina module
    private boolean publicFlag = false; // Stores if the public flag is enabled for the binding generation
    private String outputPath; // Output path of the bindings generated
    private String packageName; // Ballerina project's current package name
    private Path projectRoot; // Ballerina project root
    private TomlDocument tomlDocument; // TomlDocument object representing the Ballerina.toml file

    // Flag depicting whether the current class being generated is a direct class or a dependent class
    private boolean directJavaClass = true;
    private Set<String> classPaths = new HashSet<>();
    private Map<String, String> aliases = new HashMap<>();
    private Set<String> classListForLooping = new HashSet<>();
    private Set<String> allJavaClasses = new HashSet<>();
    private Set<JError> exceptionList = new HashSet<>();
    private Map<String, String> failedClassGens = new HashMap<>();
    private List<String> failedMethodGens = new ArrayList<>();
    private Set<String> superClasses = new HashSet<>();

    public void setModulesFlag(boolean modulesFlag) {
        this.modulesFlag = modulesFlag;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setProject(Project project) {
        this.projectRoot = project.sourceRoot();
        if (project.currentPackage() != null) {
            Package currentPackage = project.currentPackage();
            this.packageName = currentPackage.packageName().toString();
            if (currentPackage.ballerinaToml().isPresent()) {
                this.tomlDocument = currentPackage.ballerinaToml().get().tomlDocument();
            }
        }
    }

    String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public TomlDocument getTomlDocument() {
        return tomlDocument;
    }

    public Path getProjectRoot() {
        return projectRoot;
    }

    public void setSuperClasses(Class superClass) {
        Class parent = superClass;
        while (parent != null && isPublicClass(parent)) {
            this.superClasses.add(parent.getName());
            parent = parent.getSuperclass();
        }
    }

    public Set<String> getSuperClasses() {
        return this.superClasses;
    }

    public boolean getModulesFlag() {
        return modulesFlag;
    }

    public boolean isDirectJavaClass() {
        return directJavaClass;
    }

    public void setDirectJavaClass(boolean directJavaClass) {
        this.directJavaClass = directJavaClass;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public Set<String> getClassPaths() {
        return classPaths;
    }

    public void addClasspath(String classpath) {
        this.classPaths.add(classpath);
    }

    public Map<String, String> getAliases() {
        return aliases;
    }

    String getAliasClassName(String alias) {
        return aliases.get(alias);
    }

    /**
     * Set an alias name for a fully qualified class name.
     *
     * @param alias the alias name
     * @param className the fully qualified class name
     */
    public void setAlias(String alias, String className) {
        this.aliases.put(alias, className);
    }

    String getAlias(String className) {
        for (Map.Entry<String, String> entry : aliases.entrySet()) {
            if (className.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }

    String removeAlias(String alias) {
        return this.aliases.remove(alias);
    }

    boolean hasPublicFlag() {
        return publicFlag;
    }

    public void setPublicFlag(boolean publicFlag) {
        this.publicFlag = publicFlag;
    }

    public void setClassListForLooping(String classListForLooping) {
        if (!allJavaClasses.contains(classListForLooping)) {
            this.classListForLooping.add(classListForLooping);
        }
    }

    public Set<String> getClassListForLooping() {
        return classListForLooping;
    }

    public void clearClassListForLooping() {
        classListForLooping.clear();
    }

    public Set<String> getAllJavaClasses() {
        return allJavaClasses;
    }

    public void setAllJavaClasses(Set<String> newClasses) {
        allJavaClasses.addAll(newClasses);
    }

    public Set<JError> getExceptionList() {
        return exceptionList;
    }

    public void setExceptionList(JError jError) {
        this.exceptionList.add(jError);
    }

    public Map<String, String> getFailedClassGens() {
        return failedClassGens;
    }

    public void setFailedClassGens(String className, String errorDescription) {
        this.failedClassGens.put(className, errorDescription);
    }

    /**
     * Returns the list of output messages notifying failed method generations.
     *
     * @return list of output messages
     */
    public List<String> getFailedMethodGens() {
        return failedMethodGens;
    }

    /**
     * Add an entry to the list of output messages notifying failed method generations.
     *
     * @param errorMsg error message of a failed method generation
     */
    void setFailedMethodGens(String errorMsg) {
        this.failedMethodGens.add(errorMsg);
    }
}
