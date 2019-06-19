/*
 *  Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.ballerinalang.stdlib.task.objects;

import org.ballerinalang.connector.api.Service;
import org.ballerinalang.jvm.Strand;
import org.ballerinalang.jvm.values.ObjectValue;
import org.ballerinalang.model.values.BValue;

/**
 * Custom object to store service with the parameters to use inside the service.
 *
 * @since 0.995.0
 */
public class ServiceWithParameters {
    private Strand strand = null;
    private ObjectValue serviceObj = null;
    private Object attachmentObj = null;
    private Service service = null;
    private BValue attachment = null;

    //TODO Remove after migration : implemented using bvm values/types
    public ServiceWithParameters(Service service, BValue attachment) {
        this.service = service;
        this.attachment = attachment;
    }

    //TODO Remove after migration : implemented using bvm values/types
    public ServiceWithParameters(Service service) {
        this.service = service;
        this.attachment = null;
    }

    public ServiceWithParameters(Strand strand, ObjectValue serviceObj, Object attachmentObj) {
        this.strand = strand;
        this.serviceObj = serviceObj;
        this.attachmentObj = attachmentObj;
    }

    public ServiceWithParameters(Strand strand, ObjectValue serviceObj) {
        this.strand = strand;
        this.serviceObj = serviceObj;
        this.attachmentObj = null;
    }

    public Service getService() {
        return service;
    }

    public BValue getAttachment() {
        return this.attachment;
    }

    public ObjectValue getServiceObj() {
        return serviceObj;
    }

    public Object getAttachmentObj() {
        return this.attachmentObj;
    }

    public Strand getStrand() {
        return strand;
    }
}
