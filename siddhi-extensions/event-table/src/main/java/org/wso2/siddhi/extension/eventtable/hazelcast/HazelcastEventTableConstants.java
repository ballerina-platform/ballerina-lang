/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.siddhi.extension.eventtable.hazelcast;


public final class HazelcastEventTableConstants {
    private HazelcastEventTableConstants() {
    }

    public static final String HAZELCAST_INSTANCE_PREFIX = "org.wso2.siddhi.hazelcast.cluster.instance.";
    public static final String HAZELCAST_MAP_INSTANCE_PREFIX = "org.wso2.siddhi.hazelcast.cluster.instance.map.";
    public static final String HAZELCAST_LIST_INSTANCE_PREFIX = "org.wso2.siddhi.hazelcast.cluster.instance.list.";

    public static final String ANNOTATION_ELEMENT_INSTANCE_NAME = "instance.name";
    public static final String ANNOTATION_ELEMENT_CLUSTER_NAME = "cluster.name";
    public static final String ANNOTATION_ELEMENT_CLUSTER_PASSWORD = "cluster.password";
    public static final String ANNOTATION_ELEMENT_CLUSTER_ADDRESSES = "cluster.addresses";

    public static final int STREAM_EVENT_POOL_SIZE = 10;
}
