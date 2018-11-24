/*
*  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.ballerinalang.bre.bvm.CPU;
import org.ballerinalang.model.types.BType;
import org.ballerinalang.util.exceptions.BLangFreezeException;
import org.ballerinalang.util.exceptions.BallerinaException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * {@code BValue} represents any value in Ballerina.
 *
 * @since 0.8.0
 */
public interface BValue {

    String stringValue();

    BType getType();

    void stamp(BType type);

    /**
     * Deep copy {@link BValue}.
     *
     * @param refs Represents the reference map which is passed from the top most 'copy' invocation. It contains all
     *             the copies which were created earlier, within the current {@link BValue} object.
     * @return A copy of this {@link BValue}
     */
    BValue copy(Map<BValue, BValue> refs);

    /**
     * Method to attempt freezing a {@link BValue}, to disallow further modification.
     *
     * @param freezeStatus  the {@link org.ballerinalang.bre.bvm.CPU.FreezeStatus} instance to keep track of the
     *                      freeze result of this attempt
     */
    default void attemptFreeze(CPU.FreezeStatus freezeStatus) {
        throw new BLangFreezeException("freeze not allowed on '" + getType() + "'");
    }

    /**
     * Method to retrieve if the {@link BValue} is frozen, if applicable. Compile time checks ensure that the check
     * is only possible on structured basic types.
     *
     * @return Whether the value is frozen
     */
    default boolean isFrozen() {
        return false;
    }

    /**
     * Default serialize implementation for {@link BValue}.
     *
     * @param outputStream Represent the output stream that the data will be written to.
     */
    public default void serialize(OutputStream outputStream) {
        try {
            outputStream.write(this.stringValue().getBytes(Charset.defaultCharset()));
        } catch (IOException e) {
            throw new BallerinaException("error occurred while serializing data", e);
        }
    }
}
