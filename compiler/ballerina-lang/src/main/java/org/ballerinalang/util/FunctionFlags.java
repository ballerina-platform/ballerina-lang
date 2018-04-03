/*
*  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util;

/**
 * This represents the function related flags. The flags are set/get using bit masks. 
 */
public class FunctionFlags {

    public static final int NOTHING = 0;
    public static final int ASYNC = 1;
    public static final int OBSERVED = ASYNC << 1;
    
    public static boolean isAsync(int flags) {
        return (flags & FunctionFlags.ASYNC) == FunctionFlags.ASYNC;
    }
    
    public static boolean isObserved(int flags) {
        return (flags & FunctionFlags.OBSERVED) == FunctionFlags.OBSERVED;
    }
    
    public static int markAsync(int flags) {
        return flags | FunctionFlags.ASYNC;
    }
    
    public static int markObserved(int flags) {
        return flags | FunctionFlags.OBSERVED;
    }
    
}
