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
package org.ballerinalang.docgen.model;


import org.ballerinalang.docgen.docs.utils.BallerinaDocUtils;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Package documentation.
 */
public class PackageDoc {
    public final String description;
    public final BLangPackage bLangPackage;

    /**
     * Constructor.
     *
     * @param descriptionPath description of the package.
     * @param bLangPackage    package object.
     * @throws IOException on error.
     */
    public PackageDoc(Path descriptionPath, BLangPackage bLangPackage) throws IOException {
        this.description = getDescription(descriptionPath);
        this.bLangPackage = bLangPackage;
    }

    private String getDescription(Path descriptionPath) throws IOException {
        if (descriptionPath != null) {
            String mdContent = new String(Files.readAllBytes(descriptionPath), "UTF-8");
            return BallerinaDocUtils.mdToHtml(mdContent);
        }
        return null;
    }

}
