/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.util;

import org.ballerinalang.bre.InstructionHandler;
import org.ballerinalang.util.exceptions.BallerinaException;
import org.wso2.ballerinalang.compiler.util.Names;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Contains all the util methods for core BLang.
 *
 * @since 0.985.0
 */
public class BLangUtils {

    /**
     * Returns string array of orgName, moduleName and moduleVersion for the given full module path.
     *
     * @param fullModulePath full module path
     * @return string array of orgName, moduleName and moduleVersion
     */
    public static String[] getModulePathSlices(String fullModulePath) {
        if (fullModulePath.equals(Names.DEFAULT_PACKAGE.getValue())) {
            return new String[]{null, fullModulePath, null};
        }

        String[] moduleVersionParts = fullModulePath.split(Names.VERSION_SEPARATOR.getValue());
        if (moduleVersionParts.length != 2) {
            throw new BallerinaException("Wrong module path. Format: organizationName/moduleName:moduleVersion");
        }
        String[] moduleOrgParts = moduleVersionParts[0].split(Names.ORG_NAME_SEPARATOR.getValue());
        if (moduleOrgParts.length != 2) {
            throw new BallerinaException("Wrong module path. Format: organizationName/moduleName:moduleVersion");
        }

        return new String[]{moduleOrgParts[0], moduleOrgParts[1], moduleVersionParts[1]};
    }

    /**
     * Getter for all instruction handlers for BVM interception.
     * Instruction handlers will be set after filtering out whether to engage or not for Ip interception.
     *
     * @return list if instruction handlers
     */
    public static List<InstructionHandler> getInstructionHandlers() {
        List<InstructionHandler> instructionHandlers = new ArrayList<>();

        ServiceLoader<InstructionHandler> insHandlerServLoader = ServiceLoader.load(InstructionHandler.class);
        Iterator<InstructionHandler> insHandlerServLoaderItr = insHandlerServLoader.iterator();
        while (insHandlerServLoaderItr.hasNext()) {
            InstructionHandler instructionHandler = insHandlerServLoaderItr.next();
            if (instructionHandler.shouldEngageIn()) {
                instructionHandlers.add(instructionHandler);
            }
        }

        return instructionHandlers;
    }

}
