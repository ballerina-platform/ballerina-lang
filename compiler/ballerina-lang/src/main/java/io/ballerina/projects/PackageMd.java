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


package io.ballerina.projects;

/**
 * Represents the 'Package.md' file in a package.
 *
 * @since 2.0.0
 */
public class PackageMd {

    private final MdDocumentContext mdDocumentContext;
    private final Package packageInstance;

    PackageMd(MdDocumentContext documentContext, Package packageInstance) {
        this.mdDocumentContext = documentContext;
        this.packageInstance = packageInstance;
    }

    public static PackageMd from(DocumentConfig documentConfig, Package aPackage) {
        MdDocumentContext documentContext = MdDocumentContext.from(documentConfig);
        return new PackageMd(documentContext, aPackage);
    }

    public static PackageMd from(MdDocumentContext documentContext, Package aPackage) {
        return new PackageMd(documentContext, aPackage);
    }

    public MdDocumentContext mdDocumentContext() {
        return mdDocumentContext;
    }

    public Package packageInstance() {
        return packageInstance;
    }
}
