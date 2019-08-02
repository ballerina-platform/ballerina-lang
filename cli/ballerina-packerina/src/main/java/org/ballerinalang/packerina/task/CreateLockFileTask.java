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
import org.ballerinalang.packerina.buildcontext.BuildContextField;
import org.ballerinalang.toml.parser.ManifestProcessor;
import org.wso2.ballerinalang.compiler.LockFileWriter;
import org.wso2.ballerinalang.compiler.tree.BLangPackage;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

/**
 * Task for creating a lock file.
 */
public class CreateLockFileTask implements Task {
    @Override
    public void execute(BuildContext buildContext) {
        CompilerContext compilerContext = buildContext.get(BuildContextField.COMPILER_CONTEXT);
        LockFileWriter lockFileWriter = LockFileWriter.getInstance(compilerContext);
        lockFileWriter.writeLockFile(ManifestProcessor.getInstance(compilerContext).getManifest());
    
        for (BLangPackage module : buildContext.getModules()) {
            lockFileWriter.addEntryPkg(module.symbol);
        }
    }
}
