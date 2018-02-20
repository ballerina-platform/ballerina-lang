/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.ballerinalang.plugins.idea.ui;

import com.intellij.ide.util.PropertiesComponent;
import com.intellij.notification.NotificationListener;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.components.ApplicationComponent;
import org.ballerinalang.plugins.idea.BallerinaConstants;
import org.jetbrains.annotations.NotNull;

/**
 * Provides Ballerina notification when the user install the plugin for the first time.
 */
public class BallerinaNotification implements ApplicationComponent {

    private static final String BALLERINA_PROJECT_TUTORIAL_NOTIFICATION_SHOWN =
            "learn.ballerina.notification.shown";

    @Override
    public void initComponent() {
        PropertiesComponent propertiesComponent = PropertiesComponent.getInstance();
        boolean wasDisplayed;
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (propertiesComponent) {
            wasDisplayed = propertiesComponent.getBoolean(BALLERINA_PROJECT_TUTORIAL_NOTIFICATION_SHOWN, false);
            propertiesComponent.setValue(BALLERINA_PROJECT_TUTORIAL_NOTIFICATION_SHOWN, true);
        }

        if (wasDisplayed) {
            return;
        }

        Notifications.Bus.notify(BallerinaConstants.BALLERINA_NOTIFICATION_GROUP.createNotification(
                "Learn Ballerina",
                "Visit <a href=\"http://ballerinalang.org\">Ballerina website<a/> to learn more about Ballerina.",
                NotificationType.INFORMATION,
                NotificationListener.URL_OPENING_LISTENER));
    }

    @Override
    public void disposeComponent() {

    }

    @NotNull
    @Override
    public String getComponentName() {
        return getClass().getName();
    }
}
