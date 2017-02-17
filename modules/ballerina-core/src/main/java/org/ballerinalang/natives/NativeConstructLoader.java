/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.ballerinalang.natives;

import org.ballerinalang.model.GlobalScope;

/**
 * Interface class to be extended by all native constructs tables. {@link NativeConstructLoader} contains 
 * methods to load native contruct symbols to symbol scopes.
 */
public interface NativeConstructLoader {
    
    /**
     * Load constructs to the provided Symbol Scope.
     * 
     * @param globalScope  Symbol scope to load construct symbols
     */
    public void load(GlobalScope globalScope);

}
