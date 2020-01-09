/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.ballerinalang.jvm.streams;


import org.ballerinalang.jvm.scheduling.Strand;
import org.ballerinalang.jvm.values.StreamValue;

import java.util.Observable;
import java.util.Observer;

/**
 * The {@link StreamSubscription} is the base abstract class for {@link DefaultStreamSubscription}.
 *
 * @since 0.995.0
 */
@Deprecated
public abstract class StreamSubscription extends Observable {

    StreamSubscription(Observer observer) {
        addObserver(observer);
    }

    public void send(Strand strand, Object data) {
        setChanged();
        // we need the strand, the real record value published and the boolean saying if we have defaultable fields
        notifyObservers(new Object[]{strand, data, false});
    }

    public abstract void execute(Object[] fpParams);

    public abstract StreamValue getStream();

}
