/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.ballerinalang.jvm.values;

import org.apache.axiom.om.OMNode;
import org.apache.axiom.om.impl.llom.OMProcessingInstructionImpl;
import org.ballerinalang.jvm.StringUtils;
import org.ballerinalang.jvm.XMLNodeType;
import org.ballerinalang.jvm.types.BType;
import org.ballerinalang.jvm.types.BTypes;
import org.ballerinalang.jvm.values.api.BString;

/**
 * XML nodes containing processing instructions.
 *
 * @since 1.2.0
 */
public class XMLPi extends XMLNonElementItem {

    private String data;
    private String target;

    public XMLPi(String data, String target) {
        this.data = data;
        this.target = target;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public String getItemType() {
        return getNodeType().value();
    }

    @Override
    public String getTextValue() {
        return "";
    }

    public String getData() {
        return data;
    }

    public String getTarget() {
        return target;
    }


    @Override
    public XMLNodeType getNodeType() {
        return XMLNodeType.PI;
    }

    @Override
    public OMNode value() {
        OMProcessingInstructionImpl pi = new OMProcessingInstructionImpl();
        pi.setTarget(this.target);
        pi.setValue(this.data);
        return pi;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj instanceof XMLPi) {
            XMLPi that = (XMLPi) obj;
            return data.equals(that.data) && target.equals(that.target);
        }
        return false;
    }

    @Override
    public BString bStringValue() {
        return StringUtils.fromString(stringValue());
    }

    @Override
    public BType getType() {
        return BTypes.typeProcessingInstruction;
    }
}
