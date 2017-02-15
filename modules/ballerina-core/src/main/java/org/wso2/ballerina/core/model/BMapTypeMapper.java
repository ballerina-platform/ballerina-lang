/*
 * Copyright (c) 2017, WSO2 Inc. (http://wso2.com) All Rights Reserved.
 * <p>
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.ballerina.core.model;

import org.wso2.ballerina.core.exception.BallerinaException;
import org.wso2.ballerina.core.interpreter.Context;
import org.wso2.ballerina.core.model.symbols.BLangSymbol;
import org.wso2.ballerina.core.model.types.TypeEdge;
import org.wso2.ballerina.core.model.types.TypeLattice;
import org.wso2.ballerina.core.model.values.BValue;
import org.wso2.ballerina.core.nativeimpl.AbstractNativeTypeMapper;

import java.util.Set;

/**
 * Cast elements of Map type by identifying source type and looking for matching type mappers.
 */
public class BMapTypeMapper extends AbstractNativeTypeMapper {

    private static final String mapperName = "BMapTypeMapper";

    /**
     * The target cast type.
     */
    private BLangSymbol targetType;

    /**
     * The source type which is the type of the map.
     */
    private BLangSymbol sourceType;

    /**
     * The package level type edges that the current statement has access to.
     */
    private Set<TypeEdge> packageTypeEdges;

    /**
     * The executor used to execute resolved type mappers.
     */
    private NodeExecutor nodeExecutor;

    /**
     * Initialize Map cast mapper.
     *
     * @param targetType The target cast type
     * @param packageTypeEdges The package level type mapper edges
     */
    public BMapTypeMapper(BLangSymbol targetType, Set<TypeEdge> packageTypeEdges) {
        this.packageTypeEdges = packageTypeEdges;
        this.targetType = targetType;
        super.setStackFrameSize(1);
        super.setName(mapperName);
        super.setSymbolName(new SymbolName(mapperName));
    }

    public void setSourceType(BLangSymbol sourceType) {
        this.sourceType = sourceType;
    }

    public void setNodeExecutor(NodeExecutor nodeExecutor) {
        this.nodeExecutor = nodeExecutor;
    }

    @Override
    public BValue convert(Context context) {
        BValue result = null;
        BValue source = context.getControlStack().getCurrentFrame().values[0];

        if (sourceType.equals(targetType)) {
            result = source;
        } else {
            for (TypeEdge edge : TypeLattice.getImplicitCastLattice().getEdges()) {
                if (edge.getSource().getType().equals(sourceType) && edge.getTarget().getType().equals(targetType)) {
                    java.util.function.Function typeMapperFunction = edge.getTypeMapperFunction();

                    if (typeMapperFunction != null) {
                        result = (BValue) typeMapperFunction.apply(source);
                    } else {
                        result = nodeExecutor.visit(edge.getTypeMapper());
                    }

                    break;
                }
            }

            if (result == null) {
                for (TypeEdge edge : packageTypeEdges) {
                    if (edge.getSource().getType().equals(sourceType) &&
                            edge.getTarget().getType().equals(targetType)) {
                        result = nodeExecutor.visit(edge.getTypeMapper());
                        break;
                    }
                }
            }

            if (result == null) {
                throw new BallerinaException("incompatible types: '" + sourceType.getSymbolName().getName() +
                        "' cannot be cast to  '" + targetType.getSymbolName().getName() + "'");
            }
        }

        return result;
    }

}
