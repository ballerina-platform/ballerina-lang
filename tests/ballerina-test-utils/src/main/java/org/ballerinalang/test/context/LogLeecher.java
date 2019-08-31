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
package org.ballerinalang.test.context;

/**
 * A Leecher can attach to a {@link ServerLogReader} and wait until a specific text is printed in the log.
 */
public class LogLeecher {

    private LeecherType leecherType = LeecherType.INFO;

    public String text;

    private boolean textFound = false;

    private boolean forcedExit = false;

    /**
     * Initializes the Leecher with expected log.
     *
     * @param text The log line expected
     */
    public LogLeecher(String text) {
        this.text = text;
    }

    /**
     * Initializes the Leecher with expected log.
     *
     * @param text The log line expected
     * @param leecherType type of the log leecher
     */
    public LogLeecher(String text, LeecherType leecherType) {
        this.text = text;
        this.leecherType = leecherType;
    }

    /**
     * Feed a log line to check if it matches the expected text.
     *
     * @param logLine The log line which was read
     */
    void feedLine(String logLine) {
        if (logLine.contains(text)) {
            textFound = true;
            synchronized (this) {
                this.notifyAll();
            }
        }
    }

    /**
     * Exit the wait loop forcibly.
     */
    void forceExit() {
        forcedExit = true;

        synchronized (this) {
            notifyAll();
        }
    }

    LeecherType getLeecherType() {
        return leecherType;
    }

    /**
     * Wait until a specific log is found. The log is checked 10 times upto the timeout given.
     *
     * @param timeout timeout
     * @throws BallerinaTestException if waiting is interrupted
     */
    public void waitForText(long timeout) throws BallerinaTestException {
        long startTime = System.currentTimeMillis();

        synchronized (this) {
            while (!textFound && !forcedExit) {
                try {
                    long waitingTime = timeout / 10;
                    this.wait(waitingTime);
                    if (System.currentTimeMillis() - startTime > timeout) {
                        throw new BallerinaTestException("Timeout expired waiting for matching log: " + text);
                    }
                } catch (InterruptedException e) {
                    throw new BallerinaTestException("Error waiting for text", e);
                }
            }

            if (!textFound) {
                throw new BallerinaTestException("Matching log not found prior to server shutdown for: " + text);
            }
        }
    }

    /**
     * Leecher type enum.
     */
    public enum LeecherType {
        INFO,
        ERROR
    }
}
