/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.ballerinalang.compiler.tree.clauses;

import org.ballerinalang.model.tree.NodeKind;
import org.ballerinalang.model.tree.clauses.WithinClause;
import org.wso2.ballerinalang.compiler.tree.BLangNode;
import org.wso2.ballerinalang.compiler.tree.BLangNodeVisitor;

/**
 * @since 0.965.0
 *
 * Implementation of {@link WithinClause}.
 */
public class BLangWithinClause extends BLangNode implements WithinClause {

    private String timeScale;
    private String timeDurationValue;

    @Override
    public String getTimeScale() {
        return timeScale;
    }

    @Override
    public void setTimeScale(String timeScale) {
        this.timeScale = timeScale;
    }

    @Override
    public String getTimeDurationValue() {
        return timeDurationValue;
    }

    @Override
    public void setTimeDurationValue(String timeDurationValue) {
        this.timeDurationValue = timeDurationValue;
    }

    @Override
    public void accept(BLangNodeVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * Returns the kind of this node.
     *
     * @return the kind of this node.
     */
    @Override
    public NodeKind getKind() {
        return NodeKind.WITHIN;
    }
}
