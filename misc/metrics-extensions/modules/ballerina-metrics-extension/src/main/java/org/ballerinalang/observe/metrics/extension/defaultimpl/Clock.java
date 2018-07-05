/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.ballerinalang.observe.metrics.extension.defaultimpl;

/**
 * Clock to measure absolute and relative time.
 *
 * @since 0.980.0
 */
public interface Clock {

    Clock DEFAULT = new Clock() {
        /**
         * Returns the current time in milliseconds. Uses {@link System#currentTimeMillis()}.
         *
         * @return time in milliseconds
         */
        @Override
        public long getCurrentTime() {
            return System.currentTimeMillis();
        }

        /**
         * Returns the current time tick. Uses {@link System#nanoTime()}
         *
         * @return time tick in nanoseconds
         */
        @Override
        public long getCurrentTick() {
            return System.nanoTime();
        }
    };

    /**
     * Get the current wall clock time. This is useful to measure time durations comparing to wall clock time.
     * Use {@link #getCurrentTick()} to measure durations.
     *
     * @return Current system time
     */
    long getCurrentTime();

    /**
     * Get current time tick from a monotonic clock source. This is useful to measure durations by calculating the
     * difference between two sample time ticks.
     *
     * @return Monotonic time.
     */
    long getCurrentTick();
}
