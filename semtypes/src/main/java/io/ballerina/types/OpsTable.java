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
package io.ballerina.types;

import io.ballerina.types.typeops.BooleanOps;
import io.ballerina.types.typeops.DecimalOps;
import io.ballerina.types.typeops.ErrorOps;
import io.ballerina.types.typeops.FloatOps;
import io.ballerina.types.typeops.FunctionOps;
import io.ballerina.types.typeops.IntOps;
import io.ballerina.types.typeops.ListTypeRoOps;
import io.ballerina.types.typeops.ListTypeRwOps;
import io.ballerina.types.typeops.MappingRoOps;
import io.ballerina.types.typeops.MappingRwOps;
import io.ballerina.types.typeops.StringOps;
import io.ballerina.types.typeops.UniformTypeOpsPanicImpl;
import io.ballerina.types.typeops.XmlRoOps;
import io.ballerina.types.typeops.XmlRwOps;

/**
 * Lookup table containing subtype ops for each uniform type indexed by uniform type code.
 *
 * @since 3.0.0
 */
public class OpsTable {
    private static final UniformTypeOpsPanicImpl PANIC_IMPL = new UniformTypeOpsPanicImpl();
    static final UniformTypeOps[] OPS;

    static {
        int i = 0;
        OPS = new UniformTypeOps[23];
        OPS[i++] = PANIC_IMPL;          // nil
        OPS[i++] = new BooleanOps();    // boolean
        OPS[i++] = new ListTypeRoOps(); // RO list
        OPS[i++] = new MappingRoOps();  // RO mapping
        OPS[i++] = new MappingRoOps();  // RO table
        OPS[i++] = new XmlRoOps();      // RO xml
        OPS[i++] = PANIC_IMPL;          // RO object
        OPS[i++] = new IntOps();        // int
        OPS[i++] = new FloatOps();      // float
        OPS[i++] = new DecimalOps();    // decimal
        OPS[i++] = new StringOps();     // string
        OPS[i++] = new ErrorOps();      // error
        OPS[i++] = new FunctionOps();   // function
        OPS[i++] = PANIC_IMPL;          // typedesc
        OPS[i++] = PANIC_IMPL;          // handle
        OPS[i++] = PANIC_IMPL;          // unused
        OPS[i++] = PANIC_IMPL;          // RW future
        OPS[i++] = PANIC_IMPL;          // RW stream
        OPS[i++] = new ListTypeRwOps(); // RW list
        OPS[i++] = new MappingRwOps();  // RW mapping
        OPS[i++] = new MappingRwOps();  // RW table
        OPS[i++] = new XmlRwOps();      // RW xml
        OPS[i] = PANIC_IMPL;            // RW object
    }
}
