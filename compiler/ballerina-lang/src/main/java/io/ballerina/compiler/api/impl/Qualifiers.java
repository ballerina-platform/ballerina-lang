/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package io.ballerina.compiler.api.impl;

import io.ballerina.compiler.api.symbols.Qualifier;
import org.wso2.ballerinalang.util.Flags;

import java.util.HashSet;
import java.util.Set;

import static org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols.isFlagOn;

/**
 * Util class for mapping internal symbol flags to relevant public qualifiers.
 *
 * @since 2.0.0
 */
public class Qualifiers {

    public static Set<Qualifier> getMethodQualifiers(long flags) {
        Set<Qualifier> qualifiers = new HashSet<>();

        if (isFlagOn(flags, Flags.PUBLIC)) {
            qualifiers.add(Qualifier.PUBLIC);
        }
        if (isFlagOn(flags, Flags.PRIVATE)) {
            qualifiers.add(Qualifier.PRIVATE);
        }
        if (isFlagOn(flags, Flags.ISOLATED)) {
            qualifiers.add(Qualifier.ISOLATED);
        }
        if (isFlagOn(flags, Flags.TRANSACTIONAL)) {
            qualifiers.add(Qualifier.TRANSACTIONAL);
        }
        if (isFlagOn(flags, Flags.REMOTE)) {
            qualifiers.add(Qualifier.REMOTE);
        }
        return qualifiers;
    }
}
