/*
 *   Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
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
package org.ballerinalang.test.utils.debug;

import org.ballerinalang.model.values.BValue;
import org.ballerinalang.util.debugger.dto.BreakPointDTO;
import org.ballerinalang.util.debugger.dto.VariableDTO;

import java.util.Map;

/**
 * Test Util class with helper methods.
 *
 * @since 0.966
 */
public class Util {

    public static DebugPoint createDebugPoint(String packagePath, String fileName, int lineNo, Step nextStep,
                                              int noOfHits, Map<String, String> expressionsMap) {
        return new DebugPoint(new BreakPointDTO(packagePath, fileName, lineNo), nextStep, noOfHits, expressionsMap);
    }

    public static BreakPointDTO[] createBreakNodeLocations(String packagePath, String fileName, int... lineNos) {
        BreakPointDTO[] breakPointDTOS = new BreakPointDTO[lineNos.length];
        int i = 0;
        for (int line : lineNos) {
            breakPointDTOS[i] = new BreakPointDTO(packagePath, fileName, line);
            i++;
        }
        return breakPointDTOS;
    }

    public static VariableDTO createVariable(String name, String scope, BValue bValue) {
        VariableDTO var = new VariableDTO(name, scope);
        var.setBValue(bValue);
        return var;
    }
}
