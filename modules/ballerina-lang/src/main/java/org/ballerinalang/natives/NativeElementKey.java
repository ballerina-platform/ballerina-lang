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
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.ballerinalang.natives;

/**
 * Represents an identifier for the native element. 
 * 
 * @since 0.94
 */
public class NativeElementKey {

    private NativeElementType type;
    
    private String packageName;
    
    private String elementName;
        
    public NativeElementKey(NativeElementType type, String packageName, String elementName) {
        this.type = type;
        this.packageName = packageName;
        this.elementName = elementName;
    }

    public NativeElementType getType() {
        return type;
    }

    public String getPackageName() {
        return packageName;
    }
    
    public String getElementName() {
        return elementName;
    }
    
    @Override
    public boolean equals(Object rhs) {
        return (rhs instanceof NativeElementKey && ((NativeElementKey) rhs).packageName.equals(this.packageName) 
                && ((NativeElementKey) rhs).elementName.equals(this.elementName) 
                && ((NativeElementKey) rhs).type.equals(this.type));
    }
    
    @Override
    public int hashCode() {
        return (this.type.name() + "#" + this.packageName + "#" + this.elementName).hashCode(); 
    }
    
}
