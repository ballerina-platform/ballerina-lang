/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.test.balo;

import org.ballerinalang.launcher.util.BFileUtil;
import org.ballerinalang.packerina.BuilderUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class BaloCreator {

    public static void create(Path projectPath, String packageId) throws IOException {
        Path baloPath = Paths.get(".ballerina");
        projectPath = Paths.get("src", "test", "resources").resolve(projectPath);
        
        // Clear any old balos
        // clearing from .ballerina will remove the .ballerina file as well. Therefore start clearing from
        // another level down
        BFileUtil.delete(projectPath.resolve(baloPath).resolve("repo"));
        
        // compile and create the balo
        BuilderUtils.compileAndWrite(projectPath, packageId, "target/libs/", false, true);

        // copy the balo to the temp-ballerina-home/libs/
        BFileUtil.copy(projectPath.resolve(baloPath), Paths.get("target", "libs"));
    }
}
