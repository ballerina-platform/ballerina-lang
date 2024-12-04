/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.ballerinalang.compiler.semantics.model.types;

import io.ballerina.types.Core;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import org.ballerinalang.model.Name;
import org.ballerinalang.model.types.SelectivelyImmutableReferenceType;
import org.ballerinalang.model.types.TypeKind;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import static org.wso2.ballerinalang.compiler.semantics.analyzer.Types.AND_READONLY_SUFFIX;

/**
 * Represents Builtin Subtype of integer.
 *
 * @since 1.2.0
 */
public class BXMLSubType extends BType implements SelectivelyImmutableReferenceType {

    public static final BXMLSubType XML_ELEMENT = new BXMLSubType(TypeTags.XML_ELEMENT, Names.XML_ELEMENT,
                                                                SemTypes.XML_ELEMENT);
    public static final BXMLSubType XML_PI = new BXMLSubType(TypeTags.XML_PI, Names.XML_PI, SemTypes.XML_PI);
    public static final BXMLSubType XML_COMMENT = new BXMLSubType(TypeTags.XML_COMMENT, Names.XML_COMMENT,
                                                                SemTypes.XML_COMMENT);
    public static final BXMLSubType XML_TEXT = new BXMLSubType(TypeTags.XML_TEXT, Names.XML_TEXT, Flags.READONLY,
                                                            SemTypes.XML_TEXT);

    private BXMLSubType(int tag, Name name, SemType semType) {
        this(tag, name, 0, semType);
    }

    private BXMLSubType(int tag, Name name, long flags, SemType semType) {
        super(tag, null, name, flags, semType);
    }

    public static BXMLSubType newImmutableXMLSubType(BXMLSubType xmlSubType) {
        return new BXMLSubType(xmlSubType.tag,
                Names.fromString(xmlSubType.name.getValue().concat(AND_READONLY_SUFFIX)),
                xmlSubType.getFlags() | Flags.READONLY,
                Core.intersect(xmlSubType.semType, PredefinedType.VAL_READONLY));
    }

    @Override
    public boolean isNullable() {
        return false;
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.XML;
    }

    @Override
    public String toString() {
        return Names.XML.value + Names.ALIAS_SEPARATOR + name;
    }

    @Override
    public String getQualifiedTypeName() {
        return Names.BALLERINA_ORG.value + Names.ORG_NAME_SEPARATOR.value
                + Names.LANG.value + Names.DOT.value + Names.XML.value + Names.ALIAS_SEPARATOR + name;
    }
}
