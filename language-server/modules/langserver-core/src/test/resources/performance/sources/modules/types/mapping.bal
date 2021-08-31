// Implementation specific to basic type list.

public type Field [string, SemType];

public type MappingAtomicType readonly & record {|
    // sorted
    string[] names;
    SemType[] types;
    SemType rest;
|};

// This is mapping index 0
// Used by bddFixReadOnly
final MappingAtomicType MAPPING_SUBTYPE_RO = { names: [], types: [], rest: READONLY };

public class MappingDefinition {
    *Definition;
    private RecAtom? roRec = ();
    private RecAtom? rwRec = ();
    private SemType? semType = ();

    public function getSemType(Env env) returns SemType {
        SemType? s = self.semType;
        if s is () {
            RecAtom ro = env.recMappingAtom();
            RecAtom rw = env.recMappingAtom();
            self.roRec = ro;
            self.rwRec = rw;
            return self.createSemType(env, ro, rw);
        }
        else {
            return s;
        }
    }

    public function define(Env env, Field[] fields, SemType rest) returns SemType {
        var [names, types] = splitFields(fields);
        MappingAtomicType rwType = {
            names: names.cloneReadOnly(),
            types: types.cloneReadOnly(),
            rest
        };
        Atom rw;
        RecAtom? rwRec = self.rwRec;
        if rwRec != () {
            rw = rwRec;
            env.setRecMappingAtomType(rwRec, rwType);
        }
        else {
            rw = env.mappingAtom(rwType);
        }
        Atom ro;
        if typeListIsReadOnly(rwType.types) && isReadOnly(rest) {
            RecAtom? roRec = self.roRec;
            if roRec == () {
                // share the definitions
                ro = rw;
            }
            else {
                ro = roRec;
                env.setRecMappingAtomType(roRec, rwType);
            }
        }
        else {
            MappingAtomicType roType = {
                names: rwType.names,
                types: readOnlyTypeList(rwType.types),
                rest: intersect(rest, READONLY)
            };
            ro = env.mappingAtom(roType);
            RecAtom? roRec = self.roRec;
            if roRec != () {
                env.setRecMappingAtomType(roRec, roType);
            }
        }
        return self.createSemType(env, ro, rw);
    }
    
    private function createSemType(Env env, Atom ro, Atom rw) returns SemType {
        BddNode roBdd = bddAtom(ro);
        BddNode rwBdd;
        if atomCmp(ro, rw) == 0 {
            // share the BDD
            rwBdd = roBdd;
        }
        else {
            rwBdd = bddAtom(rw);
        }
        SemType s = createComplexSemType(0, [[UT_MAPPING_RO, roBdd], [UT_MAPPING_RW, rwBdd]]);
        self.semType = s; 
        return s;
    }       
}

function splitFields(Field[] fields) returns [string[], SemType[]] {
    Field[] sortedFields = fields.sort("ascending", fieldName);
    string[] names = [];
    SemType[] types = [];
    foreach var [s, t] in sortedFields {
        names.push(s);
        types.push(t);
    }
    return [names, types];
}

isolated function fieldName(Field f) returns string {
    return f[0];
}

function mappingRoSubtypeIsEmpty(TypeCheckContext tc, SubtypeData t) returns boolean {
    return mappingSubtypeIsEmpty(tc, bddFixReadOnly(<Bdd>t));
}

function mappingSubtypeIsEmpty(TypeCheckContext tc, SubtypeData t) returns boolean {
    Bdd b = <Bdd>t;
    BddMemo? mm = tc.mappingMemo[b];
    BddMemo m;
    if mm is () {
        m = { bdd: b };
        tc.mappingMemo.add(m);
    }
    else {
        m = mm;
        boolean? res = m.isEmpty;
        if res is () {
            // we've got a loop
            // XXX is this right???
            return true;
        }
        else {
            return res;
        }
    }
    boolean isEmpty = bddEvery(tc, b, (), (), mappingFormulaIsEmpty);
    m.isEmpty = isEmpty;
    return isEmpty;    
}

// This works the same as the tuple case, except that instead of
// just comparing the lengths of the tuples we compare the sorted list of field names
function mappingFormulaIsEmpty(TypeCheckContext tc, Conjunction? posList, Conjunction? negList) returns boolean {
    TempMappingSubtype combined;
    if posList is () {
        combined = {
            types: [],
            names: [],
            // This isn't right for the readonly case.
            // bddFixReadOnly avoids this
            rest: TOP
        };
    }
    else {
        // combine all the positive atoms using intersection
        combined = tc.mappingAtomType(posList.atom);
        Conjunction? p = posList.next;
        while true {
            if p is () {
                break;
            }
            else {
                var m = intersectMapping(combined, tc.mappingAtomType(p.atom));
                if m is () {
                    return true;
                }
                else {
                    combined = m;
                }
                p = p.next;
            }
        }
        foreach var t in combined.types {
            if isEmpty(tc, t) {
                return true;
            }
        }
       
    }
    return !mappingInhabited(tc, combined, negList);
}

function mappingInhabited(TypeCheckContext tc, TempMappingSubtype pos, Conjunction? negList) returns boolean {
    if negList is () {
        return true;
    }
    else {
        MappingAtomicType neg = tc.mappingAtomType(negList.atom);

        MappingPairing pairing;

        if pos.names != neg.names {
            // If this negative type has required fields that the positive one does not allow
            // or vice-versa, then this negative type has no effect,
            // so we can move on to the next one

            // Deal the easy case of two closed records fast.
            if isNever(pos.rest) && isNever(neg.rest) {
                return mappingInhabited(tc, pos, negList.next);
            }
            pairing = new (pos, neg);
            foreach var {type1: posType, type2: negType} in pairing {
                if isNever(posType) || isNever(negType) {
                    return mappingInhabited(tc, pos, negList.next);
                }
            }
            pairing.reset();
        }
        else {
            pairing = new (pos, neg);
        }

        if !isEmpty(tc, diff(pos.rest, neg.rest)) {
            return true;
        }
        foreach var {name, type1: posType, type2: negType} in pairing {
            SemType d = diff(posType, negType);
            if !isEmpty(tc, d) {
                TempMappingSubtype mt;
                int? i = pairing.index1(name);
                if i is () {
                    // the posType came from the rest type
                    mt = insertField(pos, name, d);
                }
                else {
                    SemType[] posTypes = shallowCopyTypes(pos.types);
                    posTypes[i] = d;
                    mt = { types: posTypes, names: pos.names, rest: pos.rest };
                }
                if mappingInhabited(tc, mt, negList.next) {
                    return true;
                }
            }          
        }
        return false; 
    }
}

function insertField(TempMappingSubtype m, string name, SemType t) returns TempMappingSubtype {
    string[] names = shallowCopyStrings(m.names);
    SemType[] types = shallowCopyTypes(m.types);
    int i = names.length();
    while true {
        if i == 0 || name <= names[i - 1] {
            names[i] = name;
            types[i] = t;
            break;
        }
        names[i] = names[i - 1];
        types[i] = types[i - 1];
        i -= 1;
    }
    return { names, types, rest: m.rest };
}

type TempMappingSubtype record {|
    // sorted
    string[] names;
    SemType[] types;
    SemType rest;
|};

function intersectMapping(TempMappingSubtype m1, TempMappingSubtype m2) returns TempMappingSubtype? {
    string[] names = [];
    SemType[] types = [];
    foreach var { name, type1, type2 } in new MappingPairing(m1, m2) {
        names.push(name);
        SemType t = intersect(type1, type2);
        if isNever(t) {
            return ();
        }
        types.push(t);
    }
    SemType rest = intersect(m1.rest, m2.rest);
    return { names, types, rest };
}

type FieldPair record {|
    string name;
    SemType type1;
    SemType type2;
|};

public type MappingPairIterator object {
    public function next() returns record {| FieldPair value; |}?;
};

class MappingPairing {
    *MappingPairIterator;
    *object:Iterable;
    private final string[] names1;
    private final string[] names2;
    private final SemType[] types1;
    private final SemType[] types2;
    private final int len1;
    private final int len2;
    private int i1 = 0;
    private int i2 = 0;
    private final SemType rest1;
    private final SemType rest2;

    function init(TempMappingSubtype m1, TempMappingSubtype m2) {
        self.names1 = m1.names;
        self.len1 = self.names1.length();
        self.types1 = m1.types;
        self.rest1 = m1.rest;
        self.names2 = m2.names;
        self.len2 = self.names2.length();
        self.types2 = m2.types;
        self.rest2 = m2.rest;
    }

    public function iterator() returns MappingPairIterator {
        return self;
    }

    function index1(string name) returns int? {
        int i1Prev = self.i1 - 1;
        return i1Prev >= 0 && self.names1[i1Prev] == name ? i1Prev : ();
    }

    function reset() {
        self.i1 = 0;
        self.i2 = 0;
    }

    public function next() returns record {| FieldPair value; |}? {
        FieldPair p;
        if self.i1 >= self.len1 {
            if self.i2 >= self.len2 {
                return ();
            }
            p = {
                name: self.curName2(),
                type1: self.rest1,
                type2: self.curType2()
            };
            self.i2 += 1;
        }
        else if self.i2 >= self.len2 {
            p = {
                name: self.curName1(),
                type1: self.curType1(),
                type2: self.rest2
            };
            self.i1 += 1;
        }
        else {
            string name1 = self.curName1();
            string name2 = self.curName2();
            if name1 < name2 {
                p = {
                    name: name1,
                    type1: self.curType1(),
                    type2: self.rest2
                };
                self.i1 += 1;
            }          
            else if name2 < name1 {
                p = {
                    name: name2,
                    type1: self.rest1,
                    type2: self.curType2()
                };
                self.i2 += 1;
            }
            else {
                p = {
                    name: name1,
                    type1: self.curType1(),
                    type2: self.curType2()
                };
                self.i1 += 1;
                self.i2 += 1;
            }
        }
        return { value: p };
    }
    
    private function curType1() returns SemType => self.types1[self.i1];
    
    private function curType2() returns SemType => self.types2[self.i2];
    
    private function curName1() returns string => self.names1[self.i1];

    private function curName2() returns string => self.names2[self.i2];
}


final UniformTypeOps mappingRoOps = {
    union: bddSubtypeUnion,
    intersect: bddSubtypeIntersect,
    diff: bddSubtypeDiff,
    complement: bddSubtypeComplement,
    isEmpty: mappingRoSubtypeIsEmpty
};

final UniformTypeOps mappingRwOps = {
    union: bddSubtypeUnion,
    intersect: bddSubtypeIntersect,
    diff: bddSubtypeDiff,
    complement: bddSubtypeComplement,
    isEmpty: mappingSubtypeIsEmpty
};

