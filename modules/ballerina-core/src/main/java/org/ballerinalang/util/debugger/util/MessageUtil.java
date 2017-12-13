/*
*  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.util.debugger.util;

import org.ballerinalang.model.util.JsonGenerator;
import org.ballerinalang.model.util.JsonNode;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.util.debugger.DebugConstants;
import org.ballerinalang.util.debugger.dto.BreakPointDTO;
import org.ballerinalang.util.debugger.dto.CommandDTO;
import org.ballerinalang.util.debugger.dto.FrameDTO;
import org.ballerinalang.util.debugger.dto.MessageDTO;
import org.ballerinalang.util.debugger.dto.VariableDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains util methods to convert data objects to json and vise versa.
 *
 * @since 0.95.5
 */
public class MessageUtil {
    private static final Logger log = LoggerFactory.getLogger(MessageUtil.class);
    public static final String CODE = "code";
    public static final String MESSAGE = "message";
    public static final String THREAD_ID = "threadId";
    public static final String FRAMES = "frames";
    public static final String LOCATION = "location";
    public static final String PACKAGE_PATH = "packagePath";
    public static final String FILE_NAME = "fileName";
    public static final String LINE_NUMBER = "lineNumber";
    public static final String PACKAGE_NAME = "packageName";
    public static final String FRAME_NAME = "frameName";
    public static final String LINE_ID = "lineID";
    public static final String VARIABLES = "variables";
    public static final String SCOPE = "scope";
    public static final String NAME = "name";
    public static final String TYPE = "type";
    public static final String VALUE = "value";
    public static final String COMMAND = "command";
    public static final String POINTS = "points";

    /**
     * Method to generate json message from the MessageDTO object instance.
     *
     * @param msg object instance.
     * @return json msg.
     */
    public static String getMsgString(MessageDTO msg) {
        if (msg == null) {
            return DebugConstants.ERROR_JSON;
        }
        StringWriter writer = new StringWriter();
        JsonGenerator gen = new JsonGenerator(writer);
        try {
            parseMessageDTO(msg, gen);
            return writer.toString();
        } catch (IOException e) {
            log.error("error parsing MessageDTO to json, " + e.getMessage(), e);
            return DebugConstants.ERROR_JSON;
        }
    }

    /**
     * Method to build CommandDTO instance with given json msg.
     *
     * @param json msg String.
     * @return object instance.
     */
    public static CommandDTO buildCommandDTO(String json) {
        JsonNode node = JsonParser.parse(json);
        CommandDTO commandDTO = new CommandDTO();
        commandDTO.setCommand(node.get(COMMAND).asText());
        commandDTO.setThreadId(node.get(THREAD_ID).asText());
        commandDTO.setPoints(buildBreakPoints(node.get(POINTS)));
        return commandDTO;
    }

    private static List<BreakPointDTO> buildBreakPoints(JsonNode node) {
        if (!node.isArray()) {
            return null;
        }
        List<BreakPointDTO> bPoints = new ArrayList<>();
        for (int i = 0; i < node.size(); i++) {
            JsonNode element = node.get(i);
            BreakPointDTO bPoint = new BreakPointDTO();
            bPoint.setPackagePath(element.get(PACKAGE_PATH).asText());
            bPoint.setFileName(element.get(FILE_NAME).asText());
            bPoint.setLineNumber((int) element.get(LINE_NUMBER).longValue());
            bPoints.add(bPoint);
        }
        return bPoints;
    }

    private static void parseMessageDTO(MessageDTO msg, JsonGenerator gen) throws IOException {
        gen.startObject();

        gen.writeFieldName(CODE);
        gen.writeString(msg.getCode());

        gen.writeFieldName(MESSAGE);
        gen.writeString(msg.getMessage());

        gen.writeFieldName(THREAD_ID);
        gen.writeString(msg.getThreadId());

        gen.writeFieldName(LOCATION);
        parseBreakPoint(msg.getLocation(), gen);

        gen.writeFieldName(FRAMES);
        parseFrameList(msg.getFrames(), gen);

        gen.endObject();

    }

    private static void parseBreakPoint(BreakPointDTO bPoint, JsonGenerator gen) throws IOException {
        gen.startObject();

        gen.writeFieldName(PACKAGE_PATH);
        gen.writeString(bPoint.getPackagePath());

        gen.writeFieldName(FILE_NAME);
        gen.writeString(bPoint.getFileName());

        gen.writeFieldName(LINE_NUMBER);
        gen.writeNumber(bPoint.getLineNumber());

        gen.endObject();
    }

    private static void parseFrameList(List<FrameDTO> frames, JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        for (FrameDTO f : frames) {
            parseFrame(f, gen);
        }
        gen.writeEndArray();
    }

    private static void parseFrame(FrameDTO frame, JsonGenerator gen) throws IOException {
        gen.startObject();

        gen.writeFieldName(PACKAGE_NAME);
        gen.writeString(frame.getPackageName());

        gen.writeFieldName(FRAME_NAME);
        gen.writeString(frame.getFrameName());

        gen.writeFieldName(FILE_NAME);
        gen.writeString(frame.getFileName());

        gen.writeFieldName(LINE_ID);
        gen.writeNumber(frame.getLineID());

        gen.writeFieldName(VARIABLES);

        parseVarList(frame.getVariables(), gen);

        gen.endObject();
    }

    private static void parseVarList(List<VariableDTO> vars, JsonGenerator gen) throws IOException {
        gen.writeStartArray();
        for (VariableDTO v : vars) {
            parseVar(v, gen);
        }
        gen.writeEndArray();
    }

    private static void parseVar(VariableDTO var, JsonGenerator gen) throws IOException {
        gen.startObject();

        gen.writeFieldName(SCOPE);
        gen.writeString(var.getScope());

        gen.writeFieldName(NAME);
        gen.writeString(var.getName());

        gen.writeFieldName(TYPE);
        gen.writeString(var.getType());

        gen.writeFieldName(VALUE);
        gen.writeString(var.getValue());

        gen.endObject();
    }


}
