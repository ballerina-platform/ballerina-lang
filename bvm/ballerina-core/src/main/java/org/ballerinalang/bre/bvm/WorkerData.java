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
package org.ballerinalang.bre.bvm;

import org.ballerinalang.model.values.BRefType;

/**
 * This represents the local variables that are available to a worker. 
 * 
 * @since 0.965.0
 */
public class WorkerData {

    public long[] longRegs;
    
    public double[] doubleRegs;
    
    public String[] stringRegs;
    
    public int[] intRegs;
    
    public byte[][] byteRegs;
    
    public BRefType<?>[] refRegs;
        
}
