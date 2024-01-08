/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package io.ballerina.runtime.internal.scheduling;

import io.ballerina.runtime.api.creators.ValueCreator;
import io.ballerina.runtime.api.types.Type;
import io.ballerina.runtime.api.utils.StringUtils;
import io.ballerina.runtime.api.values.BMap;
import io.ballerina.runtime.api.values.BString;
import io.ballerina.runtime.internal.ErrorUtils;
import io.ballerina.runtime.internal.values.ErrorValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.ballerina.runtime.internal.scheduling.State.BLOCK_AND_YIELD;

/**
 * This represents a worker data channel holder that is created for each strand to hold channels required.
 *
 * @since 0.995.0
 */
public class WDChannels {

    private Map<String, WorkerDataChannel> wDChannels;
    private final List<ErrorValue> errors = new ArrayList<>();

    //TODO try to generalize this to a normal data channel, in that case we won't need these classes.

    public synchronized WorkerDataChannel getWorkerDataChannel(String name) {
        if (this.wDChannels == null) {
            this.wDChannels = new HashMap<>();
        }
        WorkerDataChannel channel = this.wDChannels.get(name);
        if (channel == null) {
            channel = new WorkerDataChannel(name);
            this.wDChannels.put(name, channel);
        }
        return channel;
    }

    public Object receiveDataMultipleChannels(Strand strand, ReceiveField[] receiveFields, Type targetType)
            throws Throwable {
        if (strand.mapResult == null) {
            strand.mapResult = ValueCreator.createMapValue(targetType);
        }
        for (ReceiveField field : receiveFields) {
            WorkerDataChannel channel = getWorkerDataChannel(field.getChannelName());
            if (!channel.isClosed()) {
                Object result = channel.tryTakeData(strand, true);
                checkAndPopulateResult(strand, field, result, channel);
            } else {
                if (channel.getState() == WorkerDataChannel.State.AUTO_CLOSED) {
                    errors.add((ErrorValue) ErrorUtils.createNoMessageError(field.getChannelName()));
                }
            }
        }
        return clearResultCache(strand, receiveFields);
    }

    private void checkAndPopulateResult(Strand strand, ReceiveField field, Object result, WorkerDataChannel channel) {
        if (result != null) {
            if (result instanceof ErrorValue errorValue) {
                errors.add(errorValue);
            } else {
                strand.mapResult.populateInitialValue(StringUtils.fromString(field.getFieldName()), result);
            }
            channel.close();
            ++strand.channelCount;
        } else {
            strand.setState(BLOCK_AND_YIELD);
        }
    }

    private Object clearResultCache(Strand strand, ReceiveField[] receiveFields) {
        if (strand.channelCount == receiveFields.length) {
            BMap<BString, Object> map = strand.mapResult;
            strand.mapResult = null;
            strand.channelCount = 0;
            return map;
        }
        return null;
    }

    public Object receiveDataAlternateChannels(Strand strand, String[] channels) throws Throwable {
        Object result = null;
        boolean allChannelsClosed = true;
        for (String channelName : channels) {
            WorkerDataChannel channel = getWorkerDataChannel(channelName);
            if (!channel.isClosed()) {
                allChannelsClosed = false;
                result = channel.tryTakeData(strand, true);
                if (result != null) {
                    result = handleNonNullResult(channels, result, channel);
                }
            } else {
                if (channel.getState() == WorkerDataChannel.State.AUTO_CLOSED) {
                    errors.add((ErrorValue) ErrorUtils.createNoMessageError(channelName));
                }
            }
        }
        return processResulAndError(strand, channels, result, allChannelsClosed);
    }

    private Object handleNonNullResult(String[] channels, Object result, WorkerDataChannel channel) {
        if (result instanceof ErrorValue errorValue) {
            errors.add(errorValue);
            channel.close();
            result = null;
        } else {
            closeChannels(channels);
        }
        return result;
    }

    private Object processResulAndError(Strand strand, String[] channels, Object result, boolean allChannelsClosed) {
        if (result == null) {
            if (errors.size() == channels.length) {
                result = errors.get(errors.size() - 1);
            } else if (!allChannelsClosed) {
                strand.setState(BLOCK_AND_YIELD);
            }
        }
        return result;
    }

    private void closeChannels(String[] channels) {
        for (String channelName : channels) {
            WorkerDataChannel channel = getWorkerDataChannel(channelName);
            channel.close();
        }
    }

}
