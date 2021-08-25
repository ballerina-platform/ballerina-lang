/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.semtype;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Env node.
 *
 * @since 2.0.0
 */
public class Env {
    private final HashMap<AtomicType, TypeAtom> atomTable;
    private final ArrayList<ListAtomicType> recListAtoms;
    private final ArrayList<MappingAtomicType> recMappingAtoms;
    private final ArrayList<FunctionAtomicType> recFunctionAtoms;

    public Env() {
        this.atomTable = new HashMap<>();
        // Set up index 0 for use by bddFixReadOnly
        this.recListAtoms = new ArrayList<>();
        this.recListAtoms.add(ListAtomicType.LIST_SUBTYPE_RO);

        this.recMappingAtoms = new ArrayList<>();
        // todo: add MAPPING_SUBTYPE_RO
        this.recFunctionAtoms = new ArrayList<>();
    }


}
