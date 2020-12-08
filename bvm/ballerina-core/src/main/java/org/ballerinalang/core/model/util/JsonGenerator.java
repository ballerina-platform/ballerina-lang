/*
 *  Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.core.model.util;

import org.ballerinalang.core.model.types.TypeTags;
import org.ballerinalang.core.model.values.BBoolean;
import org.ballerinalang.core.model.values.BDecimal;
import org.ballerinalang.core.model.values.BFloat;
import org.ballerinalang.core.model.values.BInteger;
import org.ballerinalang.core.model.values.BMap;
import org.ballerinalang.core.model.values.BStreamingJSON;
import org.ballerinalang.core.model.values.BValue;
import org.ballerinalang.core.model.values.BValueArray;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.util.Map.Entry;

/**
 * This class represents the functionality to generate the JSON constructs to be written out
 * to a given {@link OutputStream}.
 * 
 * @since 0.95.5
 */
public class JsonGenerator {
    
    private static final int DEFAULT_DEPTH = 10;

    private Writer writer;
    
    private boolean[] levelInit = new boolean[DEFAULT_DEPTH];
    
    private int currentLevel;
    
    private boolean fieldActive;
    
    private static boolean[] escChars = new boolean[93];
    
    static {
        escChars['"'] = true;
        escChars['\\'] = true;
        escChars['\b'] = true;
        escChars['\n'] = true;
        escChars['\r'] = true;
        escChars['\t'] = true;
    }
    
    public JsonGenerator(OutputStream out) {
        this(out, Charset.defaultCharset());
    }
    
    public JsonGenerator(OutputStream out, Charset charset) {
        this(new BufferedWriter(new OutputStreamWriter(out, charset)));
    }

    public JsonGenerator(Writer writer) {
        this.writer = writer;
    }

    private void setLevelInit(int index, boolean init) {
        this.checkAndResizeLevels(index);
        this.levelInit[index] = init;
    }
    
    private boolean getLevelInit(int index) {
        this.checkAndResizeLevels(index);
        return this.levelInit[index];
    }
    
    private void checkAndResizeLevels(int index) {
        if (index >= this.levelInit.length) {
            boolean[] oldLI = this.levelInit;
            this.levelInit = new boolean[(int) Math.ceil(((double) oldLI.length) * 1.5)];
            System.arraycopy(oldLI, 0, this.levelInit, 0, oldLI.length);
        }
    }
    
    private void processStartLevel() throws IOException {
        if (!this.fieldActive) {
            if (this.getLevelInit(this.currentLevel)) {
                this.writer.write(", ");
            } else {
                this.setLevelInit(this.currentLevel, true);
            }
        } else {
            this.fieldActive = false;
        }
        this.currentLevel++;
    }
    
    private void processEndLevel() {
        this.setLevelInit(this.currentLevel - 1, true);
        this.setLevelInit(this.currentLevel, false);
        this.currentLevel--;
    }
    
    private void processFieldInit() throws IOException {
        if (this.getLevelInit(this.currentLevel)) {
            this.writer.write(", ");
        } else {
            this.setLevelInit(this.currentLevel, true);
        }
        this.fieldActive = true;
    }
    
    private void processValueInit() throws IOException {
        if (this.fieldActive) {
            this.fieldActive = false;
            return;
        }
        if (this.getLevelInit(this.currentLevel)) {
            this.writer.write(", ");
        } else {
            this.setLevelInit(this.currentLevel, true);
        }
    }
    
    public void startObject() throws IOException {
        this.processStartLevel();
        this.writer.write('{');
    }
    
    public void endObject() throws IOException {
        this.writer.write('}');
        this.processEndLevel();
    }
    
    public void writeFieldName(String fieldName) throws IOException {
        this.processFieldInit();
        this.writeStringValue(fieldName);
        this.writer.write(":");
    }
    
    private void writeStringValue(String value) throws IOException {
        this.writer.write("\"");        
        int count = value.length();
        char ch;
        boolean escaped = false;
        char[] chs = value.toCharArray();
        for (int i = 0; i < count; i++) {
            ch = chs[i];
            if (ch < escChars.length && escChars[ch]) {
                escaped = true;
                break;
            }
        }        
        if (escaped) {
            this.writeStringEsc(chs);            
        } else {
            this.writer.write(chs);
        }        
        this.writer.write("\"");
    }
    
    public void writeString(String value) throws IOException {
        this.processValueInit();
        this.writeStringValue(value);
    }

    public void writeStringEsc(char[] chs) throws IOException {
        int count = chs.length;
        int index = 0;
        char ch;
        for (int i = 0; i < count; i++) {
            ch = chs[i];
            switch (ch) {
            case '"':
                this.writer.write(chs, index, i - index);
                writer.write("\\\"");
                index = i + 1;
                break;
            case '\\':
                this.writer.write(chs, index, i - index);
                writer.write("\\\\");
                index = i + 1;
                break;
            case '/':
                this.writer.write(chs, index, i - index);
                writer.write("\\/");
                index = i + 1;
                break;
            case '\b':
                this.writer.write(chs, index, i - index);
                writer.write("\\b");
                index = i + 1;
                break;
            case '\n':
                this.writer.write(chs, index, i - index);
                writer.write("\\n");
                index = i + 1;
                break;
            case '\r':
                this.writer.write(chs, index, i - index);
                writer.write("\\r");
                index = i + 1;
                break;
            case '\f':
                this.writer.write(chs, index, i - index);
                writer.write("\\f");
                index = i + 1;
                break;
            case '\t':
                this.writer.write(chs, index, i - index);
                writer.write("\\t");
                index = i + 1;
                break;
            default:
                break;
            }
        }
        
        if (count - index > 0) {
            this.writer.write(chs, index, count - index);
        }

    }
    
    public void writeNumber(long value) throws IOException {
        this.processValueInit();
        this.writer.write(Long.toString(value));
    }
    
    public void writeNumber(double value) throws IOException {
        this.processValueInit();
        this.writer.write(Double.toString(value));
    }

    public void writeNumber(BigDecimal value) throws IOException {
        this.processValueInit();
        this.writer.write(value.toString());
    }

    public void writeBoolean(boolean value) throws IOException {
        this.processValueInit();
        this.writer.write(Boolean.toString(value));
    }
    
    public void writeNull() throws IOException {
        this.processValueInit();
        this.writer.write("null");
    }
    
    public void writeStartArray() throws IOException {
        this.processStartLevel();
        this.writer.write("[");
    }
    
    public void writeEndArray() throws IOException {
        this.writer.write("]");
        this.processEndLevel();
    }
    
    public void flush() throws IOException {
        this.writer.flush();
    }

    public void serialize(BValue json) throws IOException {
        if (json == null) {
            this.writeNull();
            return;
        }

        switch (json.getType().getTag()) {
            case TypeTags.ARRAY_TAG:
                if (json instanceof BStreamingJSON) {
                    ((BStreamingJSON) json).serialize(this);
                    break;
                }
                this.writeStartArray();
                BValueArray jsonArray = (BValueArray) json;
                for (int i = 0; i < jsonArray.size(); i++) {
                    this.serialize(jsonArray.getBValue(i));
                }
                this.writeEndArray();
                break;
            case TypeTags.BOOLEAN_TAG:
                this.writeBoolean(((BBoolean) json).booleanValue());
                break;
            case TypeTags.FLOAT_TAG:
                this.writeNumber(((BFloat) json).floatValue());
                break;
            case TypeTags.DECIMAL_TAG:
                this.writeNumber(((BDecimal) json).decimalValue());
                break;
            case TypeTags.INT_TAG:
                this.writeNumber(((BInteger) json).intValue());
                break;
            case TypeTags.MAP_TAG:
            case TypeTags.JSON_TAG:
                this.startObject();
                for (Entry<Object, BValue> entry : ((BMap<Object, BValue>) json).getMap().entrySet()) {
                    this.writeFieldName(entry.getKey().toString());
                    serialize(entry.getValue());
                }
                this.endObject();
                break;
            case TypeTags.STRING_TAG:
                this.writeString(json.stringValue());
                break;
            default:
                break;
        }
    }
}
