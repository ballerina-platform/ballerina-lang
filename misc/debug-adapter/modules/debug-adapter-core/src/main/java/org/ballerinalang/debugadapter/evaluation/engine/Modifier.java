/*
 * Copyright (c) 2020, WSO2 Inc. (http://wso2.com) All Rights Reserved.
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

package org.ballerinalang.debugadapter.evaluation.engine;

import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import org.ballerinalang.debugadapter.evaluation.EvaluationException;

/**
 * Value modifier implementation.
 */
public interface Modifier {

    boolean canInspect();

    boolean canSetValue();

    /**
     * sets the value to the expression.
     */
    void setValue(Value value) throws ClassNotLoadedException, InvalidTypeException, EvaluationException;

    /**
     * @return the expected type of the expression or null is class was not loaded.
     */
    Type getExpectedType() throws ClassNotLoadedException, EvaluationException;

}
