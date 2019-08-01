/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.packerina.task;

import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.writer.BirFileWriter;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BPackageSymbol;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;
import org.wso2.ballerinalang.compiler.util.ProjectDirs;

import java.nio.file.Path;
import java.util.List;

/**
 * Task for creating bir.
 */
public class CreateBirTask implements Task {
    @Override
    public void execute(BuildContext buildContext) {
        CompilerContext context = buildContext.get(BuildContextField.COMPILER_CONTEXT);
        Path sourceRootPath = buildContext.get(BuildContextField.SOURCE_ROOT);
        
        // generate bir for modules
        BirFileWriter birFileWriter = BirFileWriter.getInstance(context);
        List<BLangPackage> modules = buildContext.getModules();
        for (BLangPackage module : modules) {
            birFileWriter.write(module, buildContext.getBirPathFromTargetCache(module.packageID));
            for (BPackageSymbol importz : module.symbol.imports) {
                writeImportBir(buildContext, importz, sourceRootPath, birFileWriter);
            }
        }
    }

    private void writeImportBir(BuildContext buildContext, BPackageSymbol importz, Path project,
                                BirFileWriter birWriter) {
        // Get the jar paths
        PackageID id = importz.pkgID;
        Path importBir;
        // Skip ballerina and ballerinax
        if (id.orgName.value.equals("ballerina") || id.orgName.value.equals("ballerinax")) {
            return;
        }
        // Look if it is a project module.
        if (ProjectDirs.isModuleExist(project, id.name.value)) {
            // If so fetch from project bir cache
            importBir = buildContext.getBirPathFromTargetCache(id);
        } else {
            // If not fetch from home bir cache.
            importBir = buildContext.getBirPathFromHomeCache(id);
        }
        birWriter.writeBIRToPath(importz.birPackageFile, id, importBir);
    }
}
