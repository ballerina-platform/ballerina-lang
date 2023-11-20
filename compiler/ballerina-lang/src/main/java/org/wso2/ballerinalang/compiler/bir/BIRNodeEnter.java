/*
 *  Copyright (c) 2023, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied. See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.wso2.ballerinalang.compiler.bir;

import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.PackageCache;
import org.wso2.ballerinalang.compiler.bir.model.BIRNode;
import org.wso2.ballerinalang.compiler.bir.writer.CPEntry;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.io.DataInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class BIRNodeEnter {

    private final PackageCache packageCache;
    private static final CompilerContext.Key<BIRNodeEnter> COMPILED_PACKAGE_BIR_NODE_ENTER_KEY =
            new CompilerContext.Key<>();

    public static BIRNodeEnter getInstance(CompilerContext context) {
        BIRNodeEnter birNodeReader = context.get(COMPILED_PACKAGE_BIR_NODE_ENTER_KEY);
        if (birNodeReader == null) {
            birNodeReader = new BIRNodeEnter(context);
        }
        return birNodeReader;
    }

    private BIRNodeEnter(CompilerContext context) {
        this.packageCache = PackageCache.getInstance(context);
    }

//    public BIRNode.BIRPackage definePackage(PackageID packageID, byte[] packageBinaryContent) {
//        BIRNode.BIRPackage birPackage = definePackage(packageID)
//    }
//
//    private BIRNode.BIRPackage definePackage(PackageID packageID, InputStream programFileInStream) {
//        try (DataInputStream dataInStream = new DataInputStream(programFileInStream)) {
//            BIR
//        }
//    }

    private static class BIRPackageNodeEnv {

        PackageID requestedPackageID;
        HashMap<Integer, byte[]> unparsedBTypeCPs = new HashMap<>();
        BIRNode.BIRPackage birPackage;
        CPEntry[] constantPool;
        private static class UnresolvedType {
            String typeSig;
            Consumer<BType> completer;

            UnresolvedType(String typeSig, Consumer<BType> completer) {
                this.typeSig = typeSig;
                this.completer = completer;
            }
        }
        BIRPackageNodeEnv(){
//            this.
        }
    }


}
