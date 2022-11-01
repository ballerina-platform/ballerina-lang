/*
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.projects.internal;

import io.ballerina.tools.text.LineRange;
import org.ballerinalang.model.elements.PackageID;
import org.wso2.ballerinalang.compiler.util.CompilerContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * A data holder for the generated IDL clients.
 *
 * @since 2.3.0
 */
public class IDLClients {
    public static final CompilerContext.Key<IDLClients> IDL_CLIENT_MAP_KEY = new CompilerContext.Key<>();
    private final Map<PackageID, Map<String, Map<LineRange, Optional<PackageID>>>> idlClientMap;

    private IDLClients(CompilerContext context) {
        context.put(IDL_CLIENT_MAP_KEY, this);
        this.idlClientMap = new HashMap<>();
    }

    public static IDLClients getInstance(CompilerContext context) {
        IDLClients idlClients = context.get(IDL_CLIENT_MAP_KEY);
        if (idlClients == null) {
            idlClients = new IDLClients(context);
        }

        return idlClients;
    }

    public Map<PackageID, Map<String, Map<LineRange, Optional<PackageID>>>> idlClientMap() {
        return idlClientMap;
    }

    public void addEntry(PackageID sourcePkgID, String sourceDoc, LineRange lineRange, PackageID clientPkgID) {
        if (!this.idlClientMap.containsKey(sourcePkgID)) {
            this.idlClientMap.put(sourcePkgID, new HashMap<>());
        }
        if (!this.idlClientMap.get(sourcePkgID).containsKey(sourceDoc)) {
            this.idlClientMap.get(sourcePkgID).put(sourceDoc, new HashMap<>());
        }
        this.idlClientMap.get(sourcePkgID).get(sourceDoc).put(lineRange, Optional.ofNullable(clientPkgID));
    }
}
