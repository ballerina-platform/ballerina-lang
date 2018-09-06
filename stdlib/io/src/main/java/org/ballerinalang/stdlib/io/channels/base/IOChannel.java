/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.ballerinalang.stdlib.io.channels.base;

import java.io.IOException;

/**
 * Represents channel interface to develop io apis.
 */
public interface IOChannel {
    /**
     * <p>
     * Specified whether the channel has reached it's end.
     * <p>
     *
     * @return true if the channel has reached it's end.
     */
    boolean hasReachedEnd();

    /**
     * <p>
     * Returns the byte channel interface.
     * </p>
     *
     * @return byte channel.
     */
    Channel getChannel();

    /**
     * <p>
     * Specifies whether the channel is selectable.
     * </p>
     *
     * @return true if the channel is selectable.
     */
    boolean isSelectable();

    /**
     * <p>
     * Specifies the id of the channel.
     * </p>
     *
     * @return unique id of the underlying byte channel.
     */
    int id();

    /**
     * <p>
     * Close the channel.
     * </p>
     *
     * @throws IOException during i/o error.
     */
    void close() throws IOException;

    /**
     * Specifies whether there're remaining content on user-space.
     *
     * @return true if there're remaining content false if not.
     */
    boolean remaining();
}
