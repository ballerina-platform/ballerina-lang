/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing,
*  software distributed under the License is distributed on an
*  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*  KIND, either express or implied.  See the License for the
*  specific language governing permissions and limitations
*  under the License.
*/
package org.ballerinalang.testerina.core;

import org.ballerinalang.util.codegen.ProgramFile;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Keep a registry of {@code {@link ProgramFile}} instances.
 * This is required to modify the runtime behavior.
 */
public class TesterinaRegistry {
    private static List<ProgramFile> programFiles = new ArrayList<>();
    private static final TesterinaRegistry instance = new TesterinaRegistry();

    public static TesterinaRegistry getInstance() {
        return instance;
    }

    public Collection<ProgramFile> getProgramFiles() {
        return Collections.unmodifiableCollection(programFiles);
    }

    public void addProgramFile(ProgramFile programFile) {
        programFiles.add(programFile);
    }
 }
