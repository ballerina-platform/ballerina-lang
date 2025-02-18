/*
 *  Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
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

import io.ballerina.types.typeops.BasicTypeOpsPanicImpl;
import io.ballerina.types.typeops.BooleanOps;
import io.ballerina.types.typeops.CellOps;
import io.ballerina.types.typeops.DecimalOps;
import io.ballerina.types.typeops.ErrorOps;
import io.ballerina.types.typeops.FloatOps;
import io.ballerina.types.typeops.FunctionOps;
import io.ballerina.types.typeops.FutureOps;
import io.ballerina.types.typeops.IntOps;
import io.ballerina.types.typeops.ListOps;
import io.ballerina.types.typeops.MappingOps;
import io.ballerina.types.typeops.ObjectOps;
import io.ballerina.types.typeops.StreamOps;
import io.ballerina.types.typeops.StringOps;
import io.ballerina.types.typeops.TableOps;
import io.ballerina.types.typeops.TypedescOps;
import io.ballerina.types.typeops.XmlOps;

/**
 * Lookup table containing subtype ops for each basic type indexed by basic type code.
 *
 * @since 2201.12.0
 */
public class OpsTable {
    private static final BasicTypeOpsPanicImpl PANIC_IMPL = new BasicTypeOpsPanicImpl();
    static final BasicTypeOps[] OPS;

    static {
        int i = 0;
        OPS = new BasicTypeOps[19];
        OPS[i++] = PANIC_IMPL;          // nil
        OPS[i++] = new BooleanOps();    // boolean
        OPS[i++] = new IntOps();        // int
        OPS[i++] = new FloatOps();      // float
        OPS[i++] = new DecimalOps();    // decimal
        OPS[i++] = new StringOps();     // string
        OPS[i++] = new ErrorOps();      // error
        OPS[i++] = new TypedescOps();   // typedesc
        OPS[i++] = PANIC_IMPL;          // handle
        OPS[i++] = new FunctionOps();   // function
        OPS[i++] = PANIC_IMPL;          // regexp
        OPS[i++] = new FutureOps();     // future
        OPS[i++] = new StreamOps();     // stream
        OPS[i++] = new ListOps();       // list
        OPS[i++] = new MappingOps();    // mapping
        OPS[i++] = new TableOps();      // table
        OPS[i++] = new XmlOps();        // xml
        OPS[i++] = new ObjectOps();     // object
        OPS[i] = new CellOps();         // cell
    }
}
