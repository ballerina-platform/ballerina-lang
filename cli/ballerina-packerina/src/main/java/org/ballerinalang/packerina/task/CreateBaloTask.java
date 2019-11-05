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

import org.ballerinalang.packerina.buildcontext.BuildContext;
import org.ballerinalang.packerina.writer.BaloFileWriter;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;

import java.nio.file.Path;
import java.util.List;

/**
 * Task for creating balo file. Balo file writer is meant for modules only and not for single files.
 */
public class CreateBaloTask implements Task {
    @Override
    public void execute(BuildContext buildContext) {
        buildContext.out().println();
        buildContext.out().println("Creating balos");
        List<BLangPackage> modules = buildContext.getModules();
        for (BLangPackage module : modules) {
            Path baloPath = buildContext.getBaloFromTarget(module.packageID);
    
            // generate balo for each module.
            BaloFileWriter baloWriter = BaloFileWriter.getInstance(buildContext);
            baloWriter.write(module, baloPath, buildContext.moduleDependencyPathMap.get(module.packageID));
        }
    }
}
