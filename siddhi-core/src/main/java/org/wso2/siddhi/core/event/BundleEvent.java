/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.siddhi.core.event;

public interface BundleEvent extends ComplexEvent{


    public AtomicEvent[] getEvents();

    public AtomicEvent getEvent(int i);

    public int getActiveEvents();

    public void removeLast();

    public BundleEvent cloneEvent();

    public BundleEvent getNewInstance();

    public AtomicEvent getEvent0();

    public AtomicEvent getEvent1();

    public AtomicEvent getEvent2();

    public AtomicEvent getEvent3();

    public AtomicEvent getEvent4();

    public AtomicEvent getEvent5();

    public AtomicEvent getEvent6();

    public AtomicEvent getEvent7();

    public AtomicEvent getEvent8();

    public AtomicEvent getEvent9();
}