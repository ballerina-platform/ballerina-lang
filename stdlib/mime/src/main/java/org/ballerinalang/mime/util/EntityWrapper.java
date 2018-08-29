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

import org.ballerinalang.stdlib.io.channels.base.Channel;
import org.ballerinalang.stdlib.io.channels.base.readers.ChannelReader;
import org.ballerinalang.stdlib.io.channels.base.writers.ChannelWriter;
import org.ballerinalang.stdlib.io.utils.BallerinaIOException;

import java.io.IOException;
import java.nio.channels.WritableByteChannel;

/**
 * Wrap EntityBodyChannel using ballerina specific channel wrapper, so that ballerina IO operations work with the
 * entity body channel.
 *
 * @since 0.963.0
 */
public class EntityWrapper extends Channel {

    public EntityWrapper(EntityBodyChannel channel) throws BallerinaIOException {
        super(channel, new ChannelReader(), new ChannelWriter());
    }

    @Override
    public void transfer(int position, int count, WritableByteChannel dstChannel) throws IOException {
        //For the time being not applicable
        throw new UnsupportedOperationException();
    }

    @Override
    public Channel getChannel() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isSelectable() {
        return false;
    }

    @Override
    public boolean remaining() {
        return false;
    }
}
