/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.model;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.tree.IdentifierNode;
import org.ballerinalang.repository.PackageBinary;
import org.ballerinalang.repository.PackageRepository;
import org.ballerinalang.repository.fs.FSPackageRepository;
import org.wso2.ballerinalang.compiler.tree.BLangIdentifier;

import java.io.PrintStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * Tester class.
 */
public class BTester {
    
    public static void main(String[] args) throws Exception {
        PackageRepository repo = new FSPackageRepository(null, null, Paths.get("/home/laf/Desktop/test"));
        List<IdentifierNode> nameComps = new ArrayList<>();
        BLangIdentifier id1 = new BLangIdentifier();
        id1.setValue("a");
        BLangIdentifier id2 = new BLangIdentifier();
        id2.setValue("b");
        nameComps.add(id1);
        nameComps.add(id2);
        BLangIdentifier id3 = new BLangIdentifier();
        id3.setValue("1.0.0");
        PackageID pkgId = new PackageID(nameComps, id3);
        PackageBinary pkgBinary = repo.loadPackage(pkgId);
        log("* PackageBinary: " + pkgBinary);
    }
    
    public static void log(Object value) {
        PrintStream writer = System.out;
        writer.println(value);
    }

}
