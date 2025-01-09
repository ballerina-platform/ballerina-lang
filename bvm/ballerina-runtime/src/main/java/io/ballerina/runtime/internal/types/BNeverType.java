/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.internal.types;

import io.ballerina.runtime.api.Module;
import io.ballerina.runtime.api.constants.TypeConstants;
import io.ballerina.runtime.api.types.NeverType;
import io.ballerina.runtime.api.types.TypeTags;

/**
 * {@code BNeverType} represents the type of a {@code Never}.
 *
 * @since 2.0.0-preview1
 */
public class BNeverType extends BNullType implements NeverType {
    /**
     * Create a {@code BNeverType} represents the type of a {@code Never}.
     *
     * @param pkg package path
     */
    public BNeverType(Module pkg) {
        super(TypeConstants.NEVER_TNAME, pkg);
    }

    @Override
    public boolean isAnydata() {
        return true;
    }

    @Override
    public int getTag() {
        return TypeTags.NEVER_TAG;
    }
}
