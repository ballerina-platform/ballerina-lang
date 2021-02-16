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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The model that stores the details which will be passed on and updated while generating the mappings.
 *
 * @since 2.0.0
 */
public class BindgenEnv {

    private boolean modulesFlag = false; // Stores if the bindings are mapped as Java package per Ballerina module
    private String outputPath; // Output path of the bindings generated
    private Project project; // Ballerina project
    private String packageName; // Ballerina project's current package name
    private Path projectRoot; // Ballerina project root
    private TomlDocument tomlDocument; // TomlDocument object representing the Ballerina.toml file

    // Flag depicting whether the current class being generated is a direct class or a dependent class
    private boolean directJavaClass = true;

    private Set<String> classPaths = new HashSet<>();
    private Set<String> javaClasses = new HashSet<>(); // Java classes stored during a single iteration
    private Set<String> allPackages = new HashSet<>();
    private Set<String> allJavaClasses = new HashSet<>(); // Java classes collected through all the iterations
    private Set<String> classListForLooping = new HashSet<>();
    private Set<JError> exceptionList = new HashSet<>();

    private Map<String, String> failedClassGens = new HashMap<>(); // Failed class generations
    private Map<String, String> aliases = new HashMap<>(); // Aliases for Java classes with common short names

    public void setModulesFlag(boolean modulesFlag) {
        this.modulesFlag = modulesFlag;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setProject(Project project) {
        this.project = project;
        this.projectRoot = project.sourceRoot();
        if (project.currentPackage() != null) {
            Package currentPackage = project.currentPackage();
            this.packageName = currentPackage.packageName().toString();
            if (currentPackage.ballerinaToml().isPresent()) {
                this.tomlDocument = currentPackage.ballerinaToml().get().tomlDocument();
            }
        }
    }

    public String getPackageName() {
        return packageName;
    }

    public TomlDocument getTomlDocument() {
        return tomlDocument;
    }

    public Path getProjectRoot() {
        return projectRoot;
    }

    public void setProjectRoot(Path projectRoot) {
        this.projectRoot = projectRoot;
    }

    public Project getProject() {
        return project;
    }

    public boolean getModulesFlag() {
        return modulesFlag;
    }

    public boolean getDirectJavaClass() {
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

    public Set<String> getJavaClasses() {
        return javaClasses;
    }

    public void setJavaClasses(Set<String> javaClasses) {
        this.javaClasses = javaClasses;
    }

    public Set<String> getAllPackages() {
        return allPackages;
    }

    public void setAllPackages(Set<String> allPackages) {
        this.allPackages = allPackages;
    }

    public Set<String> getAllJavaClasses() {
        return allJavaClasses;
    }

    public void setAllJavaClasses(Set<String> allJavaClasses) {
        this.allJavaClasses = allJavaClasses;
    }

    public Set<String> getClassListForLooping() {
        return classListForLooping;
    }

    public void setClassListForLooping(Set<String> classListForLooping) {
        this.classListForLooping = classListForLooping;
    }

    public Set<JError> getExceptionList() {
        return exceptionList;
    }

    public void setExceptionList(Set<JError> exceptionList) {
        this.exceptionList = exceptionList;
    }

    public Map<String, String> getFailedClassGens() {
        return failedClassGens;
    }

    public void setFailedClassGens(Map<String, String> failedClassGens) {
        this.failedClassGens = failedClassGens;
    }

    public Map<String, String> getAliases() {
        return aliases;
    }

    public void setAliases(Map<String, String> aliases) {
        this.aliases = aliases;
    }
}
