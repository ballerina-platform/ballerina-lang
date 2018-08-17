/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.ballerinalang.toml.model;

import org.wso2.ballerinalang.compiler.LockFilePackage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Defines the LockFile object which is created using the Ballerina.lock file configs.
 *
 * @since 0.973.1
 */
public class LockFile {
    private String name = "";
    private String version = "";
    private String lockfileversion = "";
    private String ballerinaversion = "";
    private List<String> packages = new ArrayList<>();
    private List<LockFilePackage> packageList = new ArrayList<>();


    /**
     * Get the package list.
     *
     * @return package list
     */
    public List<LockFilePackage> getPackageList() {
        return packageList;
    }

    /**
     * Add a package to the package list.
     *
     * @param lockFilePackage package object
     */
    public void addPackage(LockFilePackage lockFilePackage) {
        this.packageList.add(lockFilePackage);
        packageList = removeDuplicates(packageList);
    }

    /**
     * Get lock file version.
     *
     * @return lock file version
     */
    public String getLockfileversion() {
        return lockfileversion;
    }

    /**
     * Set lock file version.
     *
     * @param lockfileversion lock file version
     */
    public void setLockfileversion(String lockfileversion) {
        this.lockfileversion = lockfileversion;
    }

    /**
     * Get packages of the project.
     *
     * @return packages packages of the project
     */
    public List<String> getPackages() {
        return packages;
    }

    /**
     * Set packages of the project.
     *
     * @param packages packages of the project
     */
    public void setPackages(List<String> packages) {
        this.packages = packages;
    }

    /**
     * Get the project name.
     *
     * @return project name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the project name.
     *
     * @param name project name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get project version.
     *
     * @return project version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Set project version.
     *
     * @param version version of the project.
     */
    public void setVersion(String version) {
        this.version = version;
    }

    /**
     * Get the ballerina version.
     *
     * @return ballerina version
     */
    public String getBallerinaVersion() {
        return ballerinaversion;
    }

    /**
     * Set the ballerina version.
     *
     * @param ballerinaversion ballerina version
     */
    public void setBallerinaVersion(String ballerinaversion) {
        this.ballerinaversion = ballerinaversion;
    }

    /**
     * Remove duplicates from packages list.
     *
     * @param list list of elements
     * @return packages list without duplicates
     */
    private List<LockFilePackage> removeDuplicates(List<LockFilePackage> list) {
        return list.stream().distinct().collect(Collectors.toList());
    }
}
