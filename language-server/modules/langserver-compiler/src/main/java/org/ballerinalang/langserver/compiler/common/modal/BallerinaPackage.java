/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.langserver.compiler.common.modal;

import org.wso2.ballerinalang.compiler.util.Names;

/**
 * Class which contains Ballerina package details.
 */
public class BallerinaPackage {
    private String packageName;
    private String orgName;
    private String version;

    public BallerinaPackage() {
        this.packageName = "";
        this.orgName = "";
        this.version = Names.DEFAULT_VERSION.getValue();
    }

    public BallerinaPackage(String orgName, String packageName, String version) {
        this.packageName = packageName;
        this.orgName = orgName;
        this.version = version;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getOrgName() {
        return orgName;
    }

    public void setOrgName(String orgName) {
        this.orgName = orgName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        String orgName = "";
        if (this.orgName != null && !this.orgName.equals(Names.ANON_ORG.getValue())) {
            orgName = this.orgName + "/";
        }

        if (version == null || version.equals(Names.DEFAULT_VERSION.getValue())
                || version.equals(Names.EMPTY.getValue())) {
            return orgName + this.packageName;
        }

        return orgName + this.packageName + ":" + this.version;
    }

    /**
     * Get the full package name alias without the package version appended.
     * @return {@link String}   Package name alias
     */
    public String getFullPackageNameAlias() {
        String orgName = "";
        if (this.orgName != null && !this.orgName.equals(Names.ANON_ORG.getValue())) {
            orgName = this.orgName + "/";
        }

        return orgName + this.packageName;
    }
}
