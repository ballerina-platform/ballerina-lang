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
 *
 */
package org.ballerinalang.stdlib.task.listener.objects;

import org.ballerinalang.bre.Context;
import org.ballerinalang.connector.api.Service;
import org.ballerinalang.stdlib.task.SchedulingException;
import org.ballerinalang.stdlib.task.listener.utils.AppointmentJob;
import org.ballerinalang.stdlib.task.listener.utils.AppointmentManager;
import org.ballerinalang.stdlib.task.listener.utils.TaskIdGenerator;
import org.ballerinalang.stdlib.task.listener.utils.TaskRegistry;
import org.quartz.SchedulerException;

import java.util.ArrayList;

/**
 * Represents an appointment.
 */
public class Appointment extends AbstractTask {
    private String id = TaskIdGenerator.generate();

    public Appointment(Context context, String cronExpression, Service service) throws SchedulingException {
        super(service);
        TaskRegistry.getInstance().addTask(this);

        try {
            AppointmentManager.getInstance().schedule(id, AppointmentJob.class, context, cronExpression);
            this.maxRuns = -1;
            this.addService(service);
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    public Appointment(Context context, String cronExpression, Service service, long maxRuns)
            throws SchedulingException {
        super(service, maxRuns);
        TaskRegistry.getInstance().addTask(this);
        this.serviceList = new ArrayList<>();

        try {
            AppointmentManager.getInstance().schedule(id, AppointmentJob.class, context, cronExpression);
            this.maxRuns = maxRuns;
            this.addService(service);
        } catch (SchedulerException e) {
            throw new SchedulingException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() throws SchedulingException {
        AppointmentManager.getInstance().stop(id);
        super.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ArrayList<Service> getServices() {
        return this.serviceList;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addService(Service service) {
        this.serviceList.add(service);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pause() throws SchedulingException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeService(Service service) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resume() throws SchedulingException {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void runServices(Context context) {
    }
}
