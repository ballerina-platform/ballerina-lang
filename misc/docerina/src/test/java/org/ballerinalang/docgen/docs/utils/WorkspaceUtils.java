/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package org.ballerinalang.docgen.docs.utils;

import org.ballerinalang.compiler.CompilerOptionName;
import org.ballerinalang.compiler.CompilerPhase;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.repository.PackageRepository;
import org.wso2.ballerinalang.compiler.Compiler;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.CompilerOptions;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.BDiagnostic;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility methods for workspace service.
 */
public class WorkspaceUtils {

    private static CompilerContext prepareCompilerContext(String fileName, String source) {
        CompilerContext context = new CompilerContext();
        List<Name> names = new ArrayList<>();
        names.add(new Name("."));
        // Registering custom PackageRepository to provide ballerina content without a file in file-system
        context.put(PackageRepository.class, new InMemoryPackageRepository(
                new PackageID(Names.ANON_ORG, names, new Name("0.0.0")),
                "", fileName, source.getBytes(StandardCharsets.UTF_8)));
        return context;
    }

    public static BLangPackage getBallerinaFileForContent(String fileName, String source) {
        CompilerContext context = prepareCompilerContext(fileName, source);
        CompilerOptions options = CompilerOptions.getInstance(context);
        options.put(CompilerOptionName.COMPILER_PHASE, CompilerPhase.DEFINE.toString());
        options.put(CompilerOptionName.PRESERVE_WHITESPACE, Boolean.TRUE.toString());
        options.put(CompilerOptionName.PROJECT_DIR, "/tmp");
        return getBallerinaPackage(fileName, context);
    }

    private static BLangPackage getBallerinaPackage(String fileName, CompilerContext context) {
        Compiler compiler = Compiler.getInstance(context);
        BLangPackage balPkg = null;
        try {
            balPkg = compiler.compile(fileName);
        } catch (Exception ex) {
            BDiagnostic catastrophic = new BDiagnostic();
            catastrophic.msg = "Failed in the runtime parse/analyze. " + ex.getMessage();
        }

        return balPkg;
    }
}
