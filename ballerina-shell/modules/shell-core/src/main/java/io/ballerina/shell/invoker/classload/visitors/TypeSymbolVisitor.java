/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License") {
        visit((TypeSymbol) symbol
;    } you may not use this file except
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

package io.ballerina.shell.invoker.classload.visitors;

import io.ballerina.compiler.api.symbols.AnyTypeSymbol;
import io.ballerina.compiler.api.symbols.AnydataTypeSymbol;
import io.ballerina.compiler.api.symbols.ArrayTypeSymbol;
import io.ballerina.compiler.api.symbols.ByteTypeSymbol;
import io.ballerina.compiler.api.symbols.ClassSymbol;
import io.ballerina.compiler.api.symbols.CompilationErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.ConstantSymbol;
import io.ballerina.compiler.api.symbols.DecimalTypeSymbol;
import io.ballerina.compiler.api.symbols.ErrorTypeSymbol;
import io.ballerina.compiler.api.symbols.FloatTypeSymbol;
import io.ballerina.compiler.api.symbols.FunctionTypeSymbol;
import io.ballerina.compiler.api.symbols.FutureTypeSymbol;
import io.ballerina.compiler.api.symbols.HandleTypeSymbol;
import io.ballerina.compiler.api.symbols.IntSigned16TypeSymbol;
import io.ballerina.compiler.api.symbols.IntSigned32TypeSymbol;
import io.ballerina.compiler.api.symbols.IntSigned8TypeSymbol;
import io.ballerina.compiler.api.symbols.IntTypeSymbol;
import io.ballerina.compiler.api.symbols.IntUnsigned16TypeSymbol;
import io.ballerina.compiler.api.symbols.IntUnsigned32TypeSymbol;
import io.ballerina.compiler.api.symbols.IntUnsigned8TypeSymbol;
import io.ballerina.compiler.api.symbols.IntersectionTypeSymbol;
import io.ballerina.compiler.api.symbols.JSONTypeSymbol;
import io.ballerina.compiler.api.symbols.MapTypeSymbol;
import io.ballerina.compiler.api.symbols.MethodSymbol;
import io.ballerina.compiler.api.symbols.NeverTypeSymbol;
import io.ballerina.compiler.api.symbols.NilTypeSymbol;
import io.ballerina.compiler.api.symbols.ObjectFieldSymbol;
import io.ballerina.compiler.api.symbols.ObjectTypeSymbol;
import io.ballerina.compiler.api.symbols.ParameterSymbol;
import io.ballerina.compiler.api.symbols.ReadonlyTypeSymbol;
import io.ballerina.compiler.api.symbols.RecordFieldSymbol;
import io.ballerina.compiler.api.symbols.RecordTypeSymbol;
import io.ballerina.compiler.api.symbols.SingletonTypeSymbol;
import io.ballerina.compiler.api.symbols.StreamTypeSymbol;
import io.ballerina.compiler.api.symbols.StringCharTypeSymbol;
import io.ballerina.compiler.api.symbols.StringTypeSymbol;
import io.ballerina.compiler.api.symbols.TableTypeSymbol;
import io.ballerina.compiler.api.symbols.TupleTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeDescTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeReferenceTypeSymbol;
import io.ballerina.compiler.api.symbols.TypeSymbol;
import io.ballerina.compiler.api.symbols.UnionTypeSymbol;
import io.ballerina.compiler.api.symbols.XMLCommentTypeSymbol;
import io.ballerina.compiler.api.symbols.XMLElementTypeSymbol;
import io.ballerina.compiler.api.symbols.XMLProcessingInstructionTypeSymbol;
import io.ballerina.compiler.api.symbols.XMLTextTypeSymbol;
import io.ballerina.compiler.api.symbols.XMLTypeSymbol;

/**
 * Visits all the types in a given type.
 * Will visit nested/parameterized/... types and perform given operations.
 *
 * @since 2.0.0
 */
public abstract class TypeSymbolVisitor {
    /**
     * Visit a leaf type symbol node.
     * Any unhandled type symbol or base symbols will be sent here.
     *
     * @param symbol Type symbol to visit.
     */
    protected abstract void visit(TypeSymbol symbol);

    protected void visit(AnydataTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(AnyTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(ArrayTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(ByteTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(ClassSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(CompilationErrorTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(ConstantSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(DecimalTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(ErrorTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(FloatTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(FunctionTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(FutureTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(HandleTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(IntersectionTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(IntSigned8TypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(IntSigned16TypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(IntSigned32TypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(IntTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(IntUnsigned8TypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(IntUnsigned16TypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(IntUnsigned32TypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(JSONTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(MapTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(NeverTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(NilTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(ObjectTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(ReadonlyTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(RecordTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(SingletonTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(StreamTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(StringCharTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(StringTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(TableTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(TupleTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(TypeDescTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(TypeReferenceTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(UnionTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(XMLCommentTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(XMLElementTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(XMLProcessingInstructionTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(XMLTextTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    protected void visit(XMLTypeSymbol symbol) {
        visit((TypeSymbol) symbol);
    }

    /**
     * Visits the type symbol given.
     * The type will be sent to the relevant visit depending on the type.
     *
     * @param symbol Symbol to visit.
     */
    protected void visitType(TypeSymbol symbol) {
        if (symbol instanceof AnydataTypeSymbol) {
            visit((AnydataTypeSymbol) symbol);
        } else if (symbol instanceof AnyTypeSymbol) {
            visit((AnyTypeSymbol) symbol);
        } else if (symbol instanceof ArrayTypeSymbol) {
            visit((ArrayTypeSymbol) symbol);
        } else if (symbol instanceof ByteTypeSymbol) {
            visit((ByteTypeSymbol) symbol);
        } else if (symbol instanceof ClassSymbol) {
            visit((ClassSymbol) symbol);
        } else if (symbol instanceof CompilationErrorTypeSymbol) {
            visit((CompilationErrorTypeSymbol) symbol);
        } else if (symbol instanceof ConstantSymbol) {
            visit((ConstantSymbol) symbol);
        } else if (symbol instanceof DecimalTypeSymbol) {
            visit((DecimalTypeSymbol) symbol);
        } else if (symbol instanceof ErrorTypeSymbol) {
            visit((ErrorTypeSymbol) symbol);
        } else if (symbol instanceof FloatTypeSymbol) {
            visit((FloatTypeSymbol) symbol);
        } else if (symbol instanceof FunctionTypeSymbol) {
            visit((FunctionTypeSymbol) symbol);
        } else if (symbol instanceof FutureTypeSymbol) {
            visit((FutureTypeSymbol) symbol);
        } else if (symbol instanceof HandleTypeSymbol) {
            visit((HandleTypeSymbol) symbol);
        } else if (symbol instanceof IntersectionTypeSymbol) {
            visit((IntersectionTypeSymbol) symbol);
        } else if (symbol instanceof IntSigned8TypeSymbol) {
            visit((IntSigned8TypeSymbol) symbol);
        } else if (symbol instanceof IntSigned16TypeSymbol) {
            visit((IntSigned16TypeSymbol) symbol);
        } else if (symbol instanceof IntSigned32TypeSymbol) {
            visit((IntSigned32TypeSymbol) symbol);
        } else if (symbol instanceof IntUnsigned8TypeSymbol) {
            visit((IntUnsigned8TypeSymbol) symbol);
        } else if (symbol instanceof IntUnsigned16TypeSymbol) {
            visit((IntUnsigned16TypeSymbol) symbol);
        } else if (symbol instanceof IntUnsigned32TypeSymbol) {
            visit((IntUnsigned32TypeSymbol) symbol);
        } else if (symbol instanceof IntTypeSymbol) {
            visit((IntTypeSymbol) symbol);
        } else if (symbol instanceof JSONTypeSymbol) {
            visit((JSONTypeSymbol) symbol);
        } else if (symbol instanceof MapTypeSymbol) {
            visit((MapTypeSymbol) symbol);
        } else if (symbol instanceof NeverTypeSymbol) {
            visit((NeverTypeSymbol) symbol);
        } else if (symbol instanceof NilTypeSymbol) {
            visit((NilTypeSymbol) symbol);
        } else if (symbol instanceof ObjectTypeSymbol) {
            visit((ObjectTypeSymbol) symbol);
        } else if (symbol instanceof ReadonlyTypeSymbol) {
            visit((ReadonlyTypeSymbol) symbol);
        } else if (symbol instanceof RecordTypeSymbol) {
            visit((RecordTypeSymbol) symbol);
        } else if (symbol instanceof SingletonTypeSymbol) {
            visit((SingletonTypeSymbol) symbol);
        } else if (symbol instanceof StreamTypeSymbol) {
            visit((StreamTypeSymbol) symbol);
        } else if (symbol instanceof StringCharTypeSymbol) {
            visit((StringCharTypeSymbol) symbol);
        } else if (symbol instanceof StringTypeSymbol) {
            visit((StringTypeSymbol) symbol);
        } else if (symbol instanceof TableTypeSymbol) {
            visit((TableTypeSymbol) symbol);
        } else if (symbol instanceof TupleTypeSymbol) {
            visit((TupleTypeSymbol) symbol);
        } else if (symbol instanceof TypeDescTypeSymbol) {
            visit((TypeDescTypeSymbol) symbol);
        } else if (symbol instanceof TypeReferenceTypeSymbol) {
            visit((TypeReferenceTypeSymbol) symbol);
        } else if (symbol instanceof UnionTypeSymbol) {
            visit((UnionTypeSymbol) symbol);
        } else if (symbol instanceof XMLCommentTypeSymbol) {
            visit((XMLCommentTypeSymbol) symbol);
        } else if (symbol instanceof XMLElementTypeSymbol) {
            visit((XMLElementTypeSymbol) symbol);
        } else if (symbol instanceof XMLProcessingInstructionTypeSymbol) {
            visit((XMLProcessingInstructionTypeSymbol) symbol);
        } else if (symbol instanceof XMLTextTypeSymbol) {
            visit((XMLTextTypeSymbol) symbol);
        } else if (symbol instanceof XMLTypeSymbol) {
            visit((XMLTypeSymbol) symbol);
        } else {
            visit(symbol);
        }
    }

    /**
     * Visit a parameter symbol.
     *
     * @param symbol Parameter symbol to visit.
     */
    protected void visitParameter(ParameterSymbol symbol) {
        throw new UnsupportedOperationException();
    }

    /**
     * Visit a object field symbol.
     *
     * @param symbol Field symbol to visit.
     */
    protected void visitField(ObjectFieldSymbol symbol) {
        throw new UnsupportedOperationException();
    }

    /**
     * Visit a object field symbol.
     *
     * @param symbol Field symbol to visit.
     */
    protected void visitField(RecordFieldSymbol symbol) {
        throw new UnsupportedOperationException();
    }

    /**
     * Visit a method declaration symbol.
     *
     * @param symbol Method symbol to visit.
     */
    protected void visitMethod(MethodSymbol symbol) {
        throw new UnsupportedOperationException();
    }
}
