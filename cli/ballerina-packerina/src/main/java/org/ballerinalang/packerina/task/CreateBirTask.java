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

import java.nio.file.Files;
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
            // If the module has a testable package we will create the bir beside it
            if (module.testablePkgs.size() > 0) {
                birFileWriter.write(module.testablePkgs.get(0),
                        buildContext.getTestBirPathFromTargetCache(module.packageID));
            }
            writeImportBir(buildContext, module.symbol.imports, sourceRootPath, birFileWriter);
        }
    }

    private void writeImportBir(BuildContext buildContext, List<BPackageSymbol> importz, Path project,
                                BirFileWriter birWriter) {
        for (BPackageSymbol bPackageSymbol : importz) {
            // Get the jar paths
            PackageID id = bPackageSymbol.pkgID;
            // Skip ballerina and ballerinax
            if (id.orgName.value.equals("ballerina") || id.orgName.value.equals("ballerinax")) {
                continue;
            }
    
            Path importBir;
            // Look if it is a project module.
            if (ProjectDirs.isModuleExist(project, id.name.value) ||
                    buildContext.getImportPathDependency(id).isPresent()) {
                // If so fetch from project bir cache
                importBir = buildContext.getBirPathFromTargetCache(id);
                birWriter.writeBIRToPath(bPackageSymbol.birPackageFile, id, importBir);
            } else {
                // If not fetch from home bir cache.
                importBir = buildContext.getBirPathFromHomeCache(id);
                // Write only if bir does not exists. No need to overwrite.
                if (Files.notExists(importBir)) {
                    birWriter.writeBIRToPath(bPackageSymbol.birPackageFile, id, importBir);
                }
            }
    
            // write child import bir(s)
            writeImportBir(buildContext, bPackageSymbol.imports, project, birWriter);
        }
    }
}
