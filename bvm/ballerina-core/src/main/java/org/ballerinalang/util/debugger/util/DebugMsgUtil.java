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

import org.apache.commons.lang3.StringEscapeUtils;
import org.ballerinalang.model.types.TypeTags;
import org.ballerinalang.model.util.JsonGenerator;
import org.ballerinalang.model.util.JsonParser;
import org.ballerinalang.model.values.BInteger;
import org.ballerinalang.model.values.BMap;
import org.ballerinalang.model.values.BRefType;
import org.ballerinalang.model.values.BRefValueArray;
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
public class DebugMsgUtil {
    private static final Logger log = LoggerFactory.getLogger(DebugMsgUtil.class);
    private static final String CODE = "code";
    private static final String MESSAGE = "message";
    private static final String THREAD_ID = "threadId";
    private static final String FRAMES = "frames";
    private static final String LOCATION = "location";
    // TODO packagePath has to be changed to modulePath. This has to be handled by the server and client.
    private static final String PACKAGE_PATH = "packagePath";
    private static final String FILE_NAME = "fileName";
    private static final String LINE_NUMBER = "lineNumber";
    private static final String PACKAGE_NAME = "packageName";
    private static final String FRAME_NAME = "frameName";
    private static final String LINE_ID = "lineID";
    private static final String VARIABLES = "variables";
    private static final String SCOPE = "scope";
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String VALUE = "value";
    private static final String COMMAND = "command";
    private static final String POINTS = "points";
    private static final String EXPRESSION = "expression";

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
            gen.flush();
            return writer.toString();
        } catch (IOException e) {
            log.error("error parsing MessageDTO to json, " + e.getMessage(), e);
            return DebugConstants.ERROR_JSON;
        }
    }

    /**
     * Method to build CommandDTO instance with given json msg.
     *
     * @param jsonStr msg String.
     * @return object instance.
     */
    public static CommandDTO buildCommandDTO(String jsonStr) {
        BRefType<?> node = JsonParser.parse(jsonStr);
        CommandDTO commandDTO = new CommandDTO();

        if (node.getType().getTag() == TypeTags.JSON_TAG) {
            BMap<String, BRefType<?>> json = (BMap) node;
            commandDTO.setCommand(json.get(COMMAND) == null ? null : json.get(COMMAND).stringValue());
            commandDTO.setThreadId(json.get(THREAD_ID) == null ? null : json.get(THREAD_ID).stringValue());
            commandDTO.setVariableName(json.get(EXPRESSION) == null ? null : json.get(EXPRESSION).stringValue());
            commandDTO.setPoints(buildBreakPoints(json.get(POINTS)));
        }
        return commandDTO;
    }

    private static List<BreakPointDTO> buildBreakPoints(BRefType<?> json) {
        if (json == null || json.getType().getTag() != TypeTags.ARRAY_TAG) {
            return null;
        }
        List<BreakPointDTO> bPoints = new ArrayList<>();
        BRefValueArray jsonArray = (BRefValueArray) json;
        for (int i = 0; i < jsonArray.size(); i++) {
            BMap<String, BRefType<?>> element = (BMap) jsonArray.get(i);
            BreakPointDTO bPoint = new BreakPointDTO();
            bPoint.setPackagePath(element.get(PACKAGE_PATH) == null ? null : element.get(PACKAGE_PATH).stringValue());
            bPoint.setFileName(element.get(FILE_NAME) == null ? null : element.get(FILE_NAME).stringValue());
            bPoint.setLineNumber(
                    element.get(LINE_NUMBER) == null ? -1 : (int) ((BInteger) element.get(LINE_NUMBER)).intValue());
            bPoints.add(bPoint);
        }
        return bPoints;
    }

    private static void parseMessageDTO(MessageDTO msg, JsonGenerator gen) throws IOException {
        gen.startObject();

        gen.writeFieldName(CODE);
        writeStringField(gen, msg.getCode());

        gen.writeFieldName(MESSAGE);
        writeStringField(gen, msg.getMessage());

        gen.writeFieldName(THREAD_ID);
        writeStringField(gen, msg.getThreadId());

        gen.writeFieldName(LOCATION);
        parseBreakPoint(msg.getLocation(), gen);

        gen.writeFieldName(FRAMES);
        parseFrameList(msg.getFrames(), gen);

        gen.endObject();

    }

    private static void parseBreakPoint(BreakPointDTO bPoint, JsonGenerator gen) throws IOException {
        if (bPoint == null) {
            gen.writeNull();
            return;
        }
        gen.startObject();

        gen.writeFieldName(PACKAGE_PATH);
        writeStringField(gen, bPoint.getPackagePath());

        gen.writeFieldName(FILE_NAME);
        writeStringField(gen, bPoint.getFileName());

        gen.writeFieldName(LINE_NUMBER);
        gen.writeNumber(bPoint.getLineNumber());

        gen.endObject();
    }

    private static void parseFrameList(List<FrameDTO> frames, JsonGenerator gen) throws IOException {
        if (frames == null) {
            gen.writeNull();
            return;
        }
        gen.writeStartArray();
        for (FrameDTO f : frames) {
            parseFrame(f, gen);
        }
        gen.writeEndArray();
    }

    private static void parseFrame(FrameDTO frame, JsonGenerator gen) throws IOException {
        if (frame == null) {
            gen.writeNull();
            return;
        }
        gen.startObject();

        gen.writeFieldName(PACKAGE_NAME);
        writeStringField(gen, frame.getPackageName());

        gen.writeFieldName(FRAME_NAME);
        writeStringField(gen, frame.getFrameName());

        gen.writeFieldName(FILE_NAME);
        writeStringField(gen, frame.getFileName());

        gen.writeFieldName(LINE_ID);
        gen.writeNumber(frame.getLineID());

        gen.writeFieldName(VARIABLES);

        parseVarList(frame.getVariables(), gen);

        gen.endObject();
    }

    private static void parseVarList(List<VariableDTO> vars, JsonGenerator gen) throws IOException {
        if (vars == null) {
            gen.writeNull();
            return;
        }
        gen.writeStartArray();
        for (VariableDTO v : vars) {
            parseVar(v, gen);
        }
        gen.writeEndArray();
    }

    private static void parseVar(VariableDTO var, JsonGenerator gen) throws IOException {
        if (var == null) {
            gen.writeNull();
            return;
        }
        gen.startObject();

        gen.writeFieldName(SCOPE);
        writeStringField(gen, var.getScope());

        gen.writeFieldName(NAME);
        writeStringField(gen, var.getName());

        gen.writeFieldName(TYPE);
        writeStringField(gen, var.getType());

        gen.writeFieldName(VALUE);
        writeStringField(gen, StringEscapeUtils.escapeJson(var.getValue()));

        gen.endObject();
    }

    private static void writeStringField(JsonGenerator gen, String value) throws IOException {
        if (value == null) {
            gen.writeNull();
            return;
        }
        gen.writeString(value);
    }
}
