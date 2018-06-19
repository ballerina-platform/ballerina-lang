/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler;

import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Compiled packages model class.
 */
public class CompiledPackages {

    private static CompiledPackages compiledPackages;
    private List<BLangPackage> list = null;

    /**
     * Compiled package constructor.
     */
    private CompiledPackages() {
        list = new ArrayList<>();
    }

    /**
     * Get an instance of the compiled package.
     *
     * @return an instance of the compiled package
     */
    public static CompiledPackages getInstance() {
        if (compiledPackages == null) {
            compiledPackages = new CompiledPackages();
        }
        return compiledPackages;
    }

    /**
     * Get compiled package list.
     *
     * @return compiled package list
     */
    public List<BLangPackage> getPkgList() {
        return this.list;
    }

    /**
     * Set compiled package list.
     *
     * @param pkgList compiled package list
     */
    void setPkgList(List<BLangPackage> pkgList) {
        this.list = pkgList;
    }

    /**
     * Clear the compiled package list.
     */
    public void clearPkgs() {
        this.list = Collections.emptyList();
    }
}
