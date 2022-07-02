/*
 * Copyright (c) 2022, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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
package io.ballerina.shell.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Collector to collect output passed in to the console.
 *
 * @since 2201.1.1
 */
public class ConsoleOutCollector extends OutputStream {
    private List<String> lines;
    private StringBuilder buffer;

    public ConsoleOutCollector() {
        this.lines = new ArrayList<>();
        this.buffer = new StringBuilder();
    }

    @Override
    public void write(int characterInt) throws IOException {
        if (characterInt == '\n') {
            lines.add(buffer.toString());
            buffer = new StringBuilder();
        } else {
            buffer.append((char) characterInt);
        }
    }

    /**
     * Returns collected strings from stream.
     *
     * @return list of collected strings.
     */
    public List<String> getLines() {
        return lines;
    }
}
