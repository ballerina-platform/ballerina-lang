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
package org.ballerinalang.bre;

/**
 * {@code GlobalVarLocation} represents a location where a variable declared in a
 * {@code BLangPackage }  is stored at runtime
 * <p>
 * Since there exists only one instance of a package, we can compute the required
 * memory block size during the compilation time.  Therefore we use the StackMemory
 * block to store all the package level variables
 *
 * @since 0.87
 */
public class GlobalVarLocation extends MemoryLocation {
    private int staticMemAddrOffset;

    public GlobalVarLocation(int staticMemAddrOffset) {
        this.staticMemAddrOffset = staticMemAddrOffset;
    }

    public int getStaticMemAddrOffset() {
        return staticMemAddrOffset;
    }
}
