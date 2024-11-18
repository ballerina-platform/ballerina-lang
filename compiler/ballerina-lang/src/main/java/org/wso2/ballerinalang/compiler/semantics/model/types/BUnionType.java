/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import io.ballerina.types.Env;
import io.ballerina.types.PredefinedType;
import io.ballerina.types.SemType;
import io.ballerina.types.SemTypes;
import org.ballerinalang.model.types.TypeKind;
import org.ballerinalang.model.types.UnionType;
import org.wso2.ballerinalang.compiler.semantics.model.TypeVisitor;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.BTypeSymbol;
import org.wso2.ballerinalang.compiler.semantics.model.symbols.Symbols;
import org.wso2.ballerinalang.compiler.util.Names;
import org.wso2.ballerinalang.compiler.util.TypeTags;
import org.wso2.ballerinalang.util.Flags;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.wso2.ballerinalang.compiler.util.TypeTags.NEVER;

/**
 * {@code UnionType} represents a union type in Ballerina.
 *
 * @since 0.966.0
 */
public class BUnionType extends BType implements UnionType {
    public boolean resolvingToString = false;
    private String cachedToString;

    protected LinkedHashSet<BType> memberTypes;

    private LinkedHashSet<SemType> memberSemTypes;

    public boolean isCyclic = false;


    private LinkedHashSet<BType> originalMemberTypes;
    private static final String INT_CLONEABLE = "__Cloneable";
    private static final String CLONEABLE = "Cloneable";
    private static final String CLONEABLE_TYPE = "CloneableType";
    private static final Pattern pCloneable = Pattern.compile(INT_CLONEABLE);
    private static final Pattern pCloneableType = Pattern.compile(CLONEABLE_TYPE);
    public final Env env;

    public BUnionType(Env env, BTypeSymbol tsymbol, LinkedHashSet<BType> memberTypes, boolean readonly) {
        this(env, tsymbol, memberTypes, memberTypes, readonly, false);
    }

    private BUnionType(Env env, BTypeSymbol tsymbol, LinkedHashSet<BType> originalMemberTypes,
                       LinkedHashSet<BType> memberTypes, boolean readonly) {
        this(env, tsymbol, originalMemberTypes, memberTypes, readonly, false);
    }

    private BUnionType(Env env, BTypeSymbol tsymbol, LinkedHashSet<BType> memberTypes,
                       boolean readonly, boolean isCyclic) {
        this(env, tsymbol, null, memberTypes, readonly, isCyclic);
    }

    private BUnionType(Env env, BTypeSymbol tsymbol, LinkedHashSet<BType> originalMemberTypes,
                       LinkedHashSet<BType> memberTypes, boolean readonly, boolean isCyclic) {
        super(TypeTags.UNION, tsymbol);

        if (readonly) {
            this.addFlags(Flags.READONLY);

            if (tsymbol != null) {
                this.tsymbol.flags |= Flags.READONLY;
            }
        }

        this.originalMemberTypes = originalMemberTypes;
        this.memberTypes = memberTypes;
        this.isCyclic = isCyclic;
        this.env = env;
    }

    @Override
    public LinkedHashSet<BType> getMemberTypes() {
        return this.memberTypes;
    }

    @Override
    public LinkedHashSet<BType> getOriginalMemberTypes() {
        return this.originalMemberTypes;
    }

    public void setMemberTypes(LinkedHashSet<BType> memberTypes) {
        assert memberTypes.isEmpty();
        this.memberTypes = memberTypes;
        this.originalMemberTypes = new LinkedHashSet<>(memberTypes);
    }

    public void setOriginalMemberTypes(LinkedHashSet<BType> memberTypes) {
        this.originalMemberTypes = new LinkedHashSet<>(memberTypes);
    }

    @Override
    public TypeKind getKind() {
        return TypeKind.UNION;
    }

    @Override
    public void accept(TypeVisitor visitor) {
        visitor.visit(this);
    }

    @Override
    public <T, R> R accept(BTypeVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }

    @Override
    public String toString() {
        if (resolvingToString) {
            return "...";
        }
        resolvingToString = true;
        computeStringRepresentation();
        resolvingToString = false;
        return cachedToString;
    }

    /**
     * Creates an empty union for cyclic union types.
     *
     * @param env     The environment to be used to create the union type.
     * @param tsymbol Type symbol for the union.
     * @param types   The types to be used to define the union.
     * @param isCyclic The cyclic indicator.
     * @return The created union type.
     */
    public static BUnionType create(Env env, BTypeSymbol tsymbol, LinkedHashSet<BType> types, boolean isCyclic) {
        LinkedHashSet<BType> memberTypes = new LinkedHashSet<>(types.size());
        boolean isImmutable = true;
        return new BUnionType(env, tsymbol, memberTypes, isImmutable, isCyclic);
    }

    /**
     * Creates a union type using the types specified in the `types` set. The created union will not have union types in
     * its member types set. If the set contains the nil type, calling isNullable() will return true.
     *
     * @param env     The environment to be used to create the union type.
     * @param tsymbol Type symbol for the union.
     * @param types   The types to be used to define the union.
     * @return The created union type.
     */
    public static BUnionType create(Env env, BTypeSymbol tsymbol, LinkedHashSet<BType> types) {
        LinkedHashSet<BType> memberTypes = new LinkedHashSet<>(types.size());

        if (types.isEmpty()) {
            return new BUnionType(env, tsymbol, memberTypes, true);
        }

        boolean isImmutable = true;
        for (BType memBType : toFlatTypeSet(types)) {
            if (!isNeverType(memBType)) {
                memberTypes.add(memBType);
            }

            if (isImmutable && !Symbols.isFlagOn(memBType.getFlags(), Flags.READONLY)) {
                isImmutable = false;
            }
        }

        if (memberTypes.isEmpty()) {
            memberTypes.add(BType.createNeverType());
            return new BUnionType(env, tsymbol, memberTypes, isImmutable);
        }

        return new BUnionType(env, tsymbol, types, memberTypes, isImmutable);
    }

    /**
     * Creates a union type using the provided types. If the set contains the nil type, calling isNullable() will return
     * true.
     *
     * @param env     The environment to be used to create the union type.
     * @param tsymbol Type symbol for the union.
     * @param types   The types to be used to define the union.
     * @return The created union type.
     */
    public static BUnionType create(Env env, BTypeSymbol tsymbol, BType... types) {
        LinkedHashSet<BType> memberTypes = new LinkedHashSet<>(types.length);
        memberTypes.addAll(Arrays.asList(types));
        return create(env, tsymbol, memberTypes);
    }

    /**
     * Adds the specified type as a member of the union. If the specified type is also a union, all the member types of
     * it are added to the union. If the newly added type is nil or is a nil-able type, the union type will also be a
     * nil-able type.
     *
     * @param type Type to be added to the union.
     */
    public void add(BType type) {
        if (type.tag == TypeTags.UNION && !isTypeParamAvailable(type)) {
            assert type instanceof BUnionType;
            BUnionType addUnion = (BUnionType) type;
            if (addUnion.isCyclic) {
                this.mergeUnionType(addUnion);
            } else {
                this.originalMemberTypes.add(addUnion);
                this.memberTypes.addAll(toFlatTypeSet(addUnion.memberTypes));
            }
        } else {
            this.originalMemberTypes.add(type);
            this.memberTypes.add(type);
        }

        if (Symbols.isFlagOn(this.getFlags(), Flags.READONLY) && !Symbols.isFlagOn(type.getFlags(), Flags.READONLY)) {
            this.setFlags(this.getFlags() ^ Flags.READONLY);
        }

        setCyclicFlag(type);
        this.semType = null; // reset cached sem-type if exists
    }

    private void setCyclicFlag(BType type) {
        if (isCyclic) {
            return;
        }

        if (type instanceof BArrayType arrayType) {
            if (arrayType.eType == this) {
                isCyclic = true;
            }
        }

        if (type instanceof BMapType mapType) {
            if (mapType.constraint == this) {
                isCyclic = true;
            }
        }

        if (type instanceof BTableType tableType) {
            if (tableType.constraint == this) {
                isCyclic = true;
            }

            if (tableType.constraint instanceof BMapType mapType) {
                if (mapType.constraint == this) {
                    isCyclic = true;
                }
            }
        }
    }

    /**
     * Adds all the types in the specified set as members of the union.
     *
     * @param types Types to be added to the union.
     */
    public void addAll(Set<BType> types) {
        types.forEach(this::add);
    }

    public void remove(BType type) {
        if (type.tag == TypeTags.UNION) {
            assert type instanceof BUnionType;
            this.memberTypes.removeAll(((BUnionType) type).getMemberTypes());
        } else {
            this.memberTypes.remove(type);
        }
        this.originalMemberTypes.remove(type);

        if (Symbols.isFlagOn(this.getFlags(), Flags.READONLY)) {
            return;
        }

        boolean isImmutable = true;
        for (BType memBType : this.memberTypes) {
            if (!Symbols.isFlagOn(memBType.getFlags(), Flags.READONLY)) {
                isImmutable = false;
                break;
            }
        }

        if (isImmutable) {
            this.addFlags(Flags.READONLY);
        }
    }

    public void mergeUnionType(BUnionType unionType) {
        if (!unionType.isCyclic) {
            for (BType member : unionType.getMemberTypes()) {
                this.add(member);
            }
            return;
        }
        this.isCyclic = true;
        for (BType member : unionType.getMemberTypes()) {
            if (member instanceof BArrayType arrayType) {
                if (getImpliedType(arrayType.eType) == unionType) {
                    BArrayType newArrayType = new BArrayType(env, this, arrayType.tsymbol, arrayType.getSize(),
                            arrayType.state, arrayType.getFlags());
                    this.add(newArrayType);
                    continue;
                }
            } else if (member instanceof BMapType mapType) {
                if (getImpliedType(mapType.constraint) == unionType) {
                    BMapType newMapType = new BMapType(env, mapType.tag, this, mapType.tsymbol, mapType.getFlags());
                    this.add(newMapType);
                    continue;
                }
            } else if (member instanceof BTableType tableType) {
                if (getImpliedType(tableType.constraint) == unionType) {
                    BTableType newTableType = new BTableType(env, tableType.tag, this, tableType.tsymbol,
                            tableType.getFlags());
                    this.add(newTableType);
                    continue;
                } else if (tableType.constraint instanceof BMapType mapType) {
                    if (getImpliedType(mapType.constraint) == unionType) {
                        BMapType newMapType = new BMapType(env, mapType.tag, this, mapType.tsymbol, mapType.getFlags());
                        BTableType newTableType = new BTableType(env, tableType.tag, newMapType, tableType.tsymbol,
                                tableType.getFlags());
                        this.add(newTableType);
                        continue;
                    }
                }
            }
            this.add(member);
        }
    }

    /**
     * Returns an iterator to iterate over the member types of the union.
     *
     * @return  An iterator over the member set.
     */
    public Iterator<BType> iterator() {
        return this.memberTypes.iterator();
    }

    public static LinkedHashSet<BType> toFlatTypeSet(LinkedHashSet<BType> types) {
        return types.stream()
                .flatMap(type -> {
                    BType refType = getImpliedType(type);
                    if (refType.tag == TypeTags.UNION && !isTypeParamAvailable(type)) {
                        return ((BUnionType) refType).memberTypes.stream();
                    }
                    return Stream.of(type);
                }).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Retrieve the referred type if a given type is a type reference type or
     * retrieve the effective type if the given type is an intersection type.
     *
     * @param type type to retrieve the implied type
     * @return the implied type if provided with a type reference type or an intersection type,
     * else returns the original type
     */
    public static BType getImpliedType(BType type) {
        type = getReferredType(type);
        if (type != null && type.tag == TypeTags.INTERSECTION) {
            return getImpliedType(((BIntersectionType) type).effectiveType);
        }

        return type;
    }

    private static BType getReferredType(BType type) {
        if (type != null && type.tag == TypeTags.TYPEREFDESC) {
            return getReferredType(((BTypeReferenceType) type).referredType);
        }

        return type;
    }

    private static boolean isTypeParamAvailable(BType type) {
        if (type.tsymbol != null && Symbols.isFlagOn(type.tsymbol.flags, Flags.TYPE_PARAM)) {
            return true;
        }
        return false;
    }

    private String getQualifiedName(String pkg, String name) {
        return (pkg.isBlank() || pkg.equals(".")) ? name : pkg + ":" + name;
    }

    private void computeStringRepresentation() {
        if (cachedToString != null) {
            return;
        }
        if (tsymbol != null && !tsymbol.getName().getValue().isEmpty()) {
            String typeName = tsymbol.getName().getValue();
            String packageId = tsymbol.pkgID.toString();
            boolean isTypeParam = Symbols.isFlagOn(getFlags(), Flags.TYPE_PARAM);
            // improve readability of cyclic union types
            if (isCyclic && (pCloneable.matcher(typeName).matches() ||
                    (isTypeParam && pCloneableType.matcher(typeName).matches()))) {
                cachedToString = getQualifiedName(packageId, CLONEABLE);
                return;
            }
            if (!isTypeParam) {
                cachedToString = getQualifiedName(packageId, typeName);
                return;
            }
        }
        LinkedHashSet<BType> uniqueTypes = new LinkedHashSet<>(originalMemberTypes.size());
        for (BType bType : originalMemberTypes) {
            if (bType.tag != TypeTags.UNION) {
                uniqueTypes.add(bType);
                continue;
            }
            BTypeSymbol tsymbol = bType.tsymbol;
            if (tsymbol != null &&  !tsymbol.getName().getValue().isEmpty()) {
                uniqueTypes.add(bType);
                continue;
            }
            uniqueTypes.addAll(((BUnionType) bType).originalMemberTypes);
        }

        StringJoiner joiner = new StringJoiner(getKind().typeName());

        boolean hasNilableMember = false;
        // This logic is added to prevent duplicate recursive calls to toString
        long numberOfNotNilTypes = 0L;
        for (BType bType : uniqueTypes) {
            if (bType.tag == TypeTags.NIL) {
                continue;
            }
            String memToString = bType.toString();

            if (bType.tag == TypeTags.UNION && memToString.startsWith("(") && memToString.endsWith(")")) {
                joiner.add(memToString.substring(1, memToString.length() - 1));
            } else {
                joiner.add(memToString);
            }
            numberOfNotNilTypes++;

            if (!hasNilableMember && bType.isNullable()) {
                hasNilableMember = true;
            }
        }

        String typeStr = numberOfNotNilTypes > 1 ? "(" + joiner + ")" : joiner.toString();
        boolean hasNilType = uniqueTypes.size() > numberOfNotNilTypes;
        cachedToString = (this.isNullable() && hasNilType && !hasNilableMember) ? (typeStr + Names.QUESTION_MARK.value)
                : typeStr;
    }

    private static boolean isNeverType(BType type) {
        if (type.tag == NEVER) {
            return true;
        } else if (type.tag == TypeTags.TYPEREFDESC) {
            return isNeverType(getImpliedType(type));
        } else if (type.tag == TypeTags.UNION) {
            for (BType memberType : ((BUnionType) type).getMemberTypes()) {
                if (!isNeverType(memberType)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public void populateMemberSemTypes(boolean ignoreTypeIds) {
        LinkedHashSet<SemType> memberSemTypes = new LinkedHashSet<>();

        for (BType memberType : this.memberTypes) {
            populateMemberSemTypes(memberType, memberSemTypes, ignoreTypeIds);
        }

        this.memberSemTypes = memberSemTypes;
        this.resetSemType();
    }

    private void populateMemberSemTypes(BType memberType, LinkedHashSet<SemType> memberSemTypes,
                                        boolean ignoreTypeIds) {
        memberType = getReferredType(memberType);
        if (memberType == null) { // TODO: handle cyclic types via BIR
            return;
        }

        if (memberType.tag == TypeTags.UNION) {
            BUnionType bUnionType = (BUnionType) memberType;
            bUnionType.populateMemberSemTypes(ignoreTypeIds);
            memberSemTypes.addAll(bUnionType.memberSemTypes);
            return;
        }

        SemType s = memberType.semType();
        if (!Core.isNever(s)) {
            memberSemTypes.add(s);
        }
    }

    /**
     * When the type is mutated we need to reset resolved semType.
     */
    public void resetSemType() {
        this.semType = null;
    }

    @Override
    public SemType semType() {
        if (this.semType == null) {
            populateMemberSemTypes(false);
            this.semType = computeResultantUnion(memberSemTypes);
        }
        return this.semType;
    }

    private SemType computeResultantUnion(LinkedHashSet<SemType> memberSemTypes) {
        SemType t = PredefinedType.NEVER;
        for (SemType s : memberSemTypes) {
            t = SemTypes.union(t, s);
        }
        return t;
    }
}
