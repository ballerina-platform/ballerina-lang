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

import io.ballerina.tools.diagnostics.Location;
import org.ballerinalang.model.elements.AttachPoint;
import org.ballerinalang.model.elements.PackageID;
import org.ballerinalang.model.symbols.AnnotationSymbol;
import org.ballerinalang.model.symbols.SymbolOrigin;
import org.wso2.ballerinalang.compiler.semantics.model.types.BType;
import org.wso2.ballerinalang.compiler.util.Name;
import org.wso2.ballerinalang.util.AttachPoints;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.wso2.ballerinalang.compiler.semantics.model.symbols.SymTag.ANNOTATION;
import static org.wso2.ballerinalang.compiler.util.CompilerUtils.getPackageIDStringWithMajorVersion;

/**
 * @since 0.94
 */
public class BAnnotationSymbol extends BTypeSymbol implements AnnotationSymbol {

    public BType attachedType;
    public Set<AttachPoint> points;
    public int maskedPoints;
    private List<BAnnotationSymbol> annots;

    public BAnnotationSymbol(Name name, Name originalName, long flags, Set<AttachPoint> points, PackageID pkgID,
                             BType type, BSymbol owner, Location pos, SymbolOrigin origin) {
        super(ANNOTATION, flags, name, originalName, pkgID, type, owner, pos, origin);
        this.points = points;
        this.maskedPoints = getMaskedPoints(points);
        this.annots = new ArrayList<>();
    }

    @Override
    public void addAnnotation(AnnotationSymbol symbol) {
        if (symbol == null) {
            return;
        }
        this.annots.add((BAnnotationSymbol) symbol);
    }

    @Override
    public List<? extends AnnotationSymbol> getAnnotations() {
        return this.annots;
    }

    @Override
    public String toString() {
        return pkgID != null && !pkgID.toString().equals(".") ?
                pkgID.toString() + ":" + this.name : this.name.toString();
    }

    public String bvmAlias() {
        String pkg = getPackageIDStringWithMajorVersion(pkgID);
        return !pkg.equals(".") ? pkg + ":" + this.name : this.name.toString();
    }

    private int getMaskedPoints(Set<AttachPoint> attachPoints) {
        Set<AttachPoint.Point> points = new HashSet<>();
        if (!attachPoints.isEmpty()) {
            for (AttachPoint attachPoint : attachPoints) {
                if (attachPoint == null) {
                    continue;
                }
                points.add(attachPoint.point);
            }
        } else {
            points = EnumSet.noneOf(AttachPoint.Point.class);
        }
        return AttachPoints.asMask(points);
    }
}
