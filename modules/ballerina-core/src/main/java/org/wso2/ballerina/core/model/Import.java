/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerina.core.model;

/**
 * {@code Import} represent an import statement
 *
 * @since 1.0.0
 */
public class Import {

    private String importName;
    private String packageName;

    public Import(String packageName) {
        this.importName = packageName.substring(packageName.lastIndexOf(".") + 1);
        this.packageName = packageName;
    }

    public Import(String importName, String packageName) {
        this.importName = importName;
        this.packageName = packageName;
    }

    /**
     * Get the name of the import
     *
     * @return name of the import
     */
    public String getImportName() {
        return importName;
    }

    /**
     * Get the imported package name
     *
     * @return name of the package imported
     */
    public String getPackageName() {
        return packageName;
    }
}
