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
package org.wso2.ballerinalang.compiler.semantics.model.symbols;

import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.AnnotationSymbol;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.diagnotic.DiagnosticPos;
import org.wso2.ballerinalang.util.AttachPoints;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag.ANNOTATION;

/**
 * @since 0.94
 */
public class BAnnotationSymbol extends BTypeSymbol implements AnnotationSymbol {

    public BTypeSymbol attachedType;
    public Set<AttachPoint> points;
    public int maskedPoints;

    public BAnnotationSymbol(Name name, int flags, Set<AttachPoint> points, PackageID pkgID,
                             BType type, BSymbol owner, DiagnosticPos pos, SymbolOrigin origin) {
        super(ANNOTATION, flags, name, pkgID, type, owner, pos, origin);
        this.points = points;
        this.maskedPoints = getMaskedPoints(points);
    }

    @Override
    public String toString() {
        return pkgID != null && !pkgID.toString().equals(".") ?
                pkgID.toString() + ":" + this.name : this.name.toString();
    }

    public String bvmAlias() {
        String pkg = pkgID.toString();
        return !pkg.equals(".") ? pkg + ":" + this.name : this.name.toString();
    }

    @Override
    public BAnnotationSymbol createLabelSymbol() {
        BAnnotationSymbol copy = Symbols.createAnnotationSymbol(flags, points, Names.EMPTY, pkgID, type, owner, pos,
                                                                origin);
        copy.attachedType = attachedType;
        copy.isLabel = true;
        return copy;
    }

    private int getMaskedPoints(Set<AttachPoint> attachPoints) {
        Set<AttachPoint.Point> points = new HashSet<>();
        if (!attachPoints.isEmpty()) {
            for (AttachPoint attachPoint : attachPoints) {
                points.add(attachPoint.point);
            }
        } else {
            points = EnumSet.noneOf(AttachPoint.Point.class);
        }
        return AttachPoints.asMask(points);
    }
}
