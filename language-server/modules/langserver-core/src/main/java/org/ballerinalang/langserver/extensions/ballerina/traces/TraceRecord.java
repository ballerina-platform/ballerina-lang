/*
 * Copyright (c) 2018, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.langserver.extensions.ballerina.traces;

import com.google.gson.JsonObject;

import java.util.UUID;

/**
 * Model class for trace log.
 */
public class TraceRecord {
    private Message message;
    private String rawMessage;
    private String id;
    private String logDate;
    private String millis;
    private String sequence;
    private String logger;
    private String level;
    private String sourceClass;
    private String sourceMethod;
    private String thread;

    TraceRecord(Message message, JsonObject record, String rawMessage) {
        this.message = message;
        this.rawMessage = rawMessage;
        this.id = UUID.randomUUID().toString();
        this.logDate = record.get("logDate").getAsString();
        this.millis = record.get("millis").getAsString();
        this.sequence = record.get("sequence").getAsString();
        this.logger = record.get("logger").getAsString();
        this.level = record.get("level").getAsString();
        this.sourceClass = record.get("sourceClass").getAsString();
        this.sourceMethod = record.get("sourceMethod").getAsString();
        this.thread = record.get("thread").getAsString();
    }

    public String getRawMessage() {
        return rawMessage;
    }

    public String getLogger() {
        return logger;
    }

    public String getSequence() {
        return sequence;
    }

    public String getThread() {
        return thread;
    }

    public String getMillis() {
        return millis;
    }

    public String getLogDate() {
        return logDate;
    }

    public String getSourceMethod() {
        return sourceMethod;
    }

    public String getSourceClass() {
        return sourceClass;
    }

    public String getLevel() {
        return level;
    }

    public Message getMessage() {
        return message;
    }

    public String getId() {
        return id;
    }
}
