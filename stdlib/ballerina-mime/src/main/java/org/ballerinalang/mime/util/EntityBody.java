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

package org.ballerinalang.mime.util;

import org.ballerinalang.nativeimpl.io.channels.FileIOChannel;

/**
 * Represent an entity body before the message data source is constructed. It can either be an EntityBodyChannel
 * or a FileIOChannel.
 *
 * @since 0.963.0
 */
public class EntityBody {
    private boolean isStream;
    private EntityWrapper entityWrapper;
    private FileIOChannel fileIOChannel;

    EntityBody(EntityWrapper entityWrapper, boolean isStream) {
        this.entityWrapper = entityWrapper;
        this.isStream = isStream;
    }

    EntityBody(FileIOChannel fileIOChannel, boolean isStream) {
        this.fileIOChannel = fileIOChannel;
        this.isStream = isStream;
    }

    public boolean isStream() {
        return isStream;
    }

    public EntityWrapper getEntityWrapper() {
        return entityWrapper;
    }

    public FileIOChannel getFileIOChannel() {
        return fileIOChannel;
    }
}
