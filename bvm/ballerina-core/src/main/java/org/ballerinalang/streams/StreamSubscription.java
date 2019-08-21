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

package org.ballerinalang.streams;


import org.ballerinalang.model.values.BStream;
import org.ballerinalang.model.values.BValue;

import java.util.Observable;
import java.util.Observer;

/**
 * The {@link StreamSubscription} is the base abstract class for {@link DefaultStreamSubscription}.
 *
 * @since 0.995.0
 */

public abstract class StreamSubscription extends Observable {

    StreamSubscription(Observer observer) {
        addObserver(observer);
    }

    public void send(BValue data) {
        setChanged();
        notifyObservers(data);
    }

    public abstract void execute(BValue data);

    public abstract BStream getStream();

}
