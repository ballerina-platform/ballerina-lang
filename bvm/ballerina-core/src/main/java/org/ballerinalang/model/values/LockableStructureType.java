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
package org.ballerinalang.model.values;

import org.ballerinalang.bre.bvm.Strand;

/**
 * The {@code LockableStructureType} represents a lockable structure type in Ballerina.
 *
 * @since 0.961.0
 */
public interface LockableStructureType extends StructureType {

    boolean lockIntField(Strand ctx, int index);

    void unlockIntField(int index);

    boolean lockFloatField(Strand ctx, int index);

    void unlockFloatField(int index);

    boolean lockStringField(Strand ctx, int index);

    void unlockStringField(int index);

    boolean lockBooleanField(Strand ctx, int index);

    void unlockBooleanField(int index);

    boolean lockRefField(Strand ctx, int index);

    void unlockRefField(int index);
}
