/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package io.ballerina.shell.cli.jline;

import io.ballerina.shell.cli.ShellExitException;
import io.ballerina.shell.cli.TerminalAdapter;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.UserInterruptException;
import org.jline.utils.AttributedStringBuilder;
import org.jline.utils.AttributedStyle;

/**
 * Terminal adapter which encapsulates Jline.
 *
 * @since 2.0.0
 */
public class JlineTerminalAdapter extends TerminalAdapter {
    protected final LineReader lineReader;

    public JlineTerminalAdapter(LineReader lineReader) {
        this.lineReader = lineReader;
    }

    @Override
    protected String color(String text, int color) {
        return new AttributedStringBuilder()
                .style(AttributedStyle.DEFAULT.foreground(color))
                .append(text).toAnsi();
    }

    @Override
    public String readLine(String prefix, String postfix) throws ShellExitException {
        try {
            return lineReader.readLine(prefix, postfix, (Character) null, null);
        } catch (UserInterruptException e) {
            return "";
        } catch (EndOfFileException e) {
            throw new ShellExitException();
        }
    }

    @Override
    public void println(String text) {
        lineReader.getTerminal().writer().println(text);
        lineReader.getTerminal().writer().flush();
    }

    @Override
    public String readOneLine(String prompt) {
        return lineReader.readLine(prompt);
    }
}
