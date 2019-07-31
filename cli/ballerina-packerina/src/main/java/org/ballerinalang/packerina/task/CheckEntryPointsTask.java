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

import org.ballerinalang.compiler.BLangCompilerException;
import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SingleFileContext;
import org.ballerinalang.packerina.buildcontext.sourcecontext.SourceType;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Task to check if there are any entry points in the modules. Fails if no entrypoints found.
 */
public class CheckEntryPointsTask implements Task {
    @Override
    public void execute(BuildContext buildContext) {
        List<BLangPackage> modulesWithEntry = buildContext.getModules().stream()
                .filter(m -> m.symbol.entryPointExists)
                .collect(Collectors.toList());
        
        if (modulesWithEntry.size() == 0) {
            StringBuilder moduleNames = new StringBuilder("[ ");
            if (buildContext.getSourceType() == SourceType.SINGLE_BAL_FILE) {
                SingleFileContext singleFileContext = buildContext.get(BuildContextField.SOURCE_CONTEXT);
                // this is to avoid spotbugs
                Path balFileName = singleFileContext.getBalFile().getFileName();
                if (null != balFileName) {
                    moduleNames.append(balFileName.toString());
                }
            } else {
                for (int i = 0; i < buildContext.getModules().size(); i++) {
                    moduleNames.append(buildContext.getModules().get(i).packageID.name.value);
                    if (i != buildContext.getModules().size() - 1) {
                        moduleNames.append(", ");
                    }
                }
            }
    
            moduleNames.append(" ]");
            
            throw new BLangCompilerException("cannot generate an executable as no entry points(main or a service) " +
                                             "were found in: " + moduleNames.toString());
        } else {
            buildContext.updateModules(modulesWithEntry);
        }
    }
}
