/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.ballerina.plugins.idea.notifiers;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationGroup;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.remoteServer.util.CloudNotifier;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

/**
 * IDEA event log notifier for ballerina home auto-detection related notifications.
 */
public class BallerinaAutoDetectNotifier extends CloudNotifier {

    private static final String notificationDisplayId = "Ballerina Home Auto Detect";

    public BallerinaAutoDetectNotifier() {
        super(notificationDisplayId);
    }

    public void showMessage(Project project, String message, MessageType messageType) {
        NotificationGroup notificationGroup = findOrCreateBaloonGroup();
        Notification notification =
                notificationGroup.createNotification("", message, messageType.toNotificationType(), null);
        if (project.isOpen() && !project.isDisposed()) {
            notification.notify(project);
        } else {
            notification.notify(null);
        }
    }

    @NotNull
    private static NotificationGroup findOrCreateBaloonGroup() {
        return Optional.ofNullable(NotificationGroup.findRegisteredGroup(
                BallerinaAutoDetectNotifier.notificationDisplayId))
                .orElseGet(() -> NotificationGroup.balloonGroup(BallerinaAutoDetectNotifier.notificationDisplayId));
    }
}
