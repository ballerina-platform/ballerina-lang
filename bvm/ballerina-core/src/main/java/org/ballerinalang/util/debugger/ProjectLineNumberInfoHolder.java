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

package org.ballerinalang.util.debugger;

import org.ballerinalang.util.codegen.LineNumberInfo;
import org.ballerinalang.util.codegen.PackageInfo;
import org.ballerinalang.util.codegen.attributes.AttributeInfo;
import org.ballerinalang.util.codegen.attributes.LineNumberTableAttributeInfo;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Initializes and holds line number info for each module for the project.
 *
 * @since 0.985.0
 *
 */
public class ProjectLineNumberInfoHolder {

    private Map<String, ModuleLineNumberInfo> packageInfoMap = new HashMap<>();

    /**
     * Process and build information required for debugging the package.
     *
     * @param packageInfos To extract relevant information.
     */
    public void processPkgInfo(PackageInfo[] packageInfos) {

        for (PackageInfo packageInfo : packageInfos) {
            ModuleLineNumberInfo moduleLineNumberInfo = new ModuleLineNumberInfo(packageInfo.getInstructionCount());

            LineNumberTableAttributeInfo lineNumberTableAttributeInfo = (LineNumberTableAttributeInfo) packageInfo
                    .getAttributeInfo(AttributeInfo.Kind.LINE_NUMBER_TABLE_ATTRIBUTE);

            List<LineNumberInfo> lineNumberInfos = lineNumberTableAttributeInfo.getLineNumberInfoList().stream().sorted(
                    Comparator.comparing(LineNumberInfo::getIp)).collect(Collectors.toList());

            LineNumberInfo currentLineNoInfo = null;
            for (LineNumberInfo lineNoInfo : lineNumberInfos) {
                if (currentLineNoInfo == null) {
                    currentLineNoInfo = lineNoInfo;
                    continue;
                }
                moduleLineNumberInfo.populateLineNumbers(currentLineNoInfo.getIp(),
                        lineNoInfo.getIp(), currentLineNoInfo);
                currentLineNoInfo = lineNoInfo;
            }
            if (currentLineNoInfo != null) {
                moduleLineNumberInfo.populateLineNumbers(currentLineNoInfo.getIp(),
                        packageInfo.getInstructionCount(), currentLineNoInfo);
            }
            packageInfoMap.put(packageInfo.getPkgPath(), moduleLineNumberInfo);
        }
    }

    public Map<String, ModuleLineNumberInfo> getPackageInfoMap() {
        return packageInfoMap;
    }
}
