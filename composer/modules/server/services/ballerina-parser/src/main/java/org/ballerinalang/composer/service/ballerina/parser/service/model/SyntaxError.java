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
package org.ballerinalang.composer.service.ballerina.parser.service.model;

import com.google.gson.JsonObject;

/**
 * Model class for a syntax error that maps to a specific line.
 */
public class SyntaxError {

    protected int row;
    protected int column;
    protected String text;
    protected String type;

    public SyntaxError(int row, int column,  String text) {
        this.text = text;
        this.column = column;
        this.row = row;
        this.type = "error";
    }

    public SyntaxError(int row, int column, String text, String type) {
        this.column = column;
        this.row = row;
        this.text = text;
        this.type = type;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public JsonObject toJson() {
        JsonObject object = new JsonObject();
        object.addProperty("row", this.row);
        object.addProperty("column", this.column);
        object.addProperty("text", this.text);
        object.addProperty("type", this.type);
        return object;
    }
}
