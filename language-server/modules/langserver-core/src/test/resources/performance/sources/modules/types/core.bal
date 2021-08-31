// There is an integer for each uniform type.
// Uniform types are like basic types except that each selectively immutable
// basic type is split into two uniform types, one immutable and on mutable.

// JBUG #31751 bad, sad if UT_OBJECT_RW + 1
public const UT_COUNT = 0x17;

const int UT_MASK = (1 << UT_COUNT) - 1;

const int UT_COUNT_RO = 0x10;
const int UT_READONLY = (1 << UT_COUNT_RO) - 1;

const int UT_RW_MASK = UT_MASK & ~UT_READONLY;


public type UniformTypeCode
    UT_NIL|UT_BOOLEAN|UT_INT|UT_FLOAT|UT_DECIMAL
    |UT_STRING|UT_ERROR|UT_FUNCTION|UT_TYPEDESC|UT_HANDLE
    |UT_XML_RO|UT_LIST_RO|UT_MAPPING_RO|UT_TABLE_RO|UT_OBJECT_RO
    |UT_XML_RW|UT_LIST_RW|UT_MAPPING_RW|UT_TABLE_RW|UT_OBJECT_RW
    |UT_STREAM|UT_FUTURE;

type Atom RecAtom|TypeAtom;

type RecAtom int;

type TypeAtom readonly & record {|
    int index;
    AtomicType atomicType;
|};

type AtomicType ListAtomicType|MappingAtomicType;

public isolated class Env {
    private final table<TypeAtom> key(atomicType) atomTable = table [];
    // Set up index 0 for use by bddFixReadOnly
    private final ListAtomicType?[] recListAtoms = [ LIST_SUBTYPE_RO ];
    private final MappingAtomicType?[] recMappingAtoms = [ MAPPING_SUBTYPE_RO ];
    private final FunctionAtomicType?[] recFunctionAtoms = [];

    public isolated function init() {
    }

    isolated function listAtom(ListAtomicType atomicType) returns TypeAtom {
        return self.typeAtom(atomicType);
    }

    isolated function mappingAtom(MappingAtomicType atomicType) returns TypeAtom {
        return self.typeAtom(atomicType);
    }

    private isolated function typeAtom(AtomicType atomicType) returns TypeAtom {
        lock {
            TypeAtom? ta = self.atomTable[atomicType];
            if ta != () {
                return ta;
            }
            else {
                TypeAtom result = { index: self.atomTable.length(), atomicType };
                self.atomTable.add(result);
                return result;
            }
        }
    }

    isolated function listAtomType(Atom atom) returns ListAtomicType {
        if atom is RecAtom {
            return self.getRecListAtomType(atom);
        }
        else {
            return <ListAtomicType>atom.atomicType;
        }
    }

    isolated function mappingAtomType(Atom atom) returns MappingAtomicType {
        if atom is RecAtom {
            return self.getRecMappingAtomType(atom);
        }
        else {
            return <MappingAtomicType>atom.atomicType;
        }
    }

    isolated function recListAtom() returns RecAtom {
        lock {
            int result = self.recListAtoms.length();
            self.recListAtoms.push(());
            return result;
        }
    }

    isolated function recMappingAtom() returns RecAtom {
        lock {
            int result = self.recMappingAtoms.length();
            self.recMappingAtoms.push(());
            return result;
        }
    }

    isolated function recFunctionAtom() returns RecAtom {
        lock {
            int result = self.recFunctionAtoms.length();
            self.recFunctionAtoms.push(());
            return result;
        }
    }

    isolated function setRecListAtomType(RecAtom ra, ListAtomicType atomicType) {
        lock {
            self.recListAtoms[ra] = atomicType;
        }
    }

    isolated function setRecMappingAtomType(RecAtom ra, MappingAtomicType atomicType) {
        lock {
            self.recMappingAtoms[ra] = atomicType;
        }
    }

    isolated function setRecFunctionAtomType(RecAtom ra, FunctionAtomicType atomicType) {
        lock {
            self.recFunctionAtoms[ra] = atomicType;
        }
    }

    isolated function getRecListAtomType(RecAtom ra) returns ListAtomicType {
        lock {
            return <ListAtomicType>self.recListAtoms[ra];
        }
    }
    
    isolated function getRecMappingAtomType(RecAtom ra) returns MappingAtomicType {
        lock {
            return <MappingAtomicType>self.recMappingAtoms[ra];
        }
    }

    isolated function getRecFunctionAtomType(RecAtom ra) returns FunctionAtomicType {
        lock {
            return <FunctionAtomicType>self.recFunctionAtoms[ra];
        }
    }
}

public type BddMemo record {|
    readonly Bdd bdd;
    boolean? isEmpty = ();
|};

type BddMemoTable table<BddMemo> key(bdd);

public class TypeCheckContext {
    private final Env env;
    BddMemoTable listMemo = table [];
    BddMemoTable mappingMemo = table [];
    BddMemoTable functionMemo = table [];
    function init(Env env) {
        self.env = env;
    }

    function listAtomType(Atom atom) returns ListAtomicType {
        if atom is RecAtom {
            return self.env.getRecListAtomType(atom);
        }
        else {
            return <ListAtomicType>atom.atomicType;
        }
    }

    function mappingAtomType(Atom atom) returns MappingAtomicType {
        if atom is RecAtom {
            return self.env.getRecMappingAtomType(atom);
        }
        else {
            return <MappingAtomicType>atom.atomicType;
        }
    }

    function functionAtomType(Atom atom) returns FunctionAtomicType {
        return self.env.getRecFunctionAtomType(<RecAtom>atom);
    }
}

type ProperSubtypeData StringSubtype|FloatSubtype|IntSubtype|BooleanSubtype|BddNode;
// true means everything and false means nothing (as with Bdd)
type SubtypeData ProperSubtypeData|boolean;

type UniformSubtype [UniformTypeCode, SubtypeData];

type BinOp function(SubtypeData t1, SubtypeData t2) returns SubtypeData;
type UnaryOp function(SubtypeData t) returns SubtypeData;
type UnaryTypeCheckOp function(TypeCheckContext tc, SubtypeData t) returns boolean;

function binOpPanic(SubtypeData t1, SubtypeData t2) returns SubtypeData {
    panic error("binary operation should not be called");
}

function unaryOpPanic(SubtypeData t) returns SubtypeData {
    panic error("unary operation should not be called");
}

function unaryTypeCheckOpPanic(TypeCheckContext tc, SubtypeData t) returns boolean {
    panic error("unary boolean operation should not be called");
}

type UniformTypeOps readonly & record {|
    BinOp union = binOpPanic;
    BinOp intersect = binOpPanic;
    BinOp diff = binOpPanic;
    UnaryOp complement = unaryOpPanic;
    UnaryTypeCheckOp isEmpty = unaryTypeCheckOpPanic;
|};

final readonly & (UniformSubtype[]) EMPTY_SUBTYPES = [];

public type UniformTypeBitSet int:Unsigned32;

public type SemType UniformTypeBitSet|ComplexSemType;

public type ComplexSemType readonly & record {|
    // For a uniform type with code c,
    // all & (1 << c) is non-zero iff this type contains all of the uniform type
    // some & (1 << c) is non-zero iff this type contains some but not all of the uniform type
    UniformTypeBitSet all;
    UniformTypeBitSet some;
    // There is one member of subtypes for each bit set in some.
    // Ordered in increasing order of UniformTypeCode
    SubtypeData[] subtypeDataList;
|};

// subtypeList must be ordered
function createComplexSemType(UniformTypeBitSet all, UniformSubtype[] subtypeList = []) returns ComplexSemType {
    int some = 0;
    SubtypeData[] dataList = [];
    foreach var [code, data] in subtypeList {
        dataList.push(data);
        int c = code;
        some |= 1 << c;
    }
    return {
        all,
        some: <UniformTypeBitSet>some,
        subtypeDataList: dataList.cloneReadOnly()
    };
}

function unpackComplexSemType(ComplexSemType t) returns UniformSubtype[] {
    int some = t.some;
    UniformSubtype[] subtypeList = [];
    foreach var data in t.subtypeDataList {
        var code = <UniformTypeCode>numberOfTrailingZeros(some);
        subtypeList.push([code, data]);
        int c = code;
        some ^= (1 << c);
    }
    return subtypeList;
}

function getComplexSubtypeData(ComplexSemType t, UniformTypeCode code) returns SubtypeData {
    int c = code;
    c = 1 << c;
    if (t.all & c) != 0 {
        return true;
    }
    if (t.some & c) == 0 {
        return false;
    }
    int loBits = t.some & (c - 1);
    return t.subtypeDataList[loBits == 0 ? 0 : bitCount(loBits)];
}

// Count number of bits set in bits.
// This is the Brian Kernighan algorithm.
// There's usually a hardware instruction for this
// typically called PopCpount
// This is __builtin_popcount in GCC and clang
// This won't work if bits is < 0.
function bitCount(int bits) returns int {
    int n = 0;
    int v = bits;
    while v != 0 {
        v &= v - 1;
        n += 1;
    }
    return n;
}


// This should be a function in lang.int
// Modern CPUs have a hardware instruction for this
// This is __builtin_ctz in GCC and clang
function numberOfTrailingZeros(int bits) returns int {
    if bits == 0 {
        return 64;
    }
    int flag = 1;
    int n = 0;
    while (bits & flag) == 0 {
        n += 1;
        flag <<= 1;
    }
    return n;
}

public function uniformType(UniformTypeCode code) returns UniformTypeBitSet {
    return <UniformTypeBitSet>(1 << code);
}

// Union of complete uniform types
// bits is bit vecor indexed by UniformTypeCode
// I would like to make the arg int:Unsigned32
// but are language/impl bugs that make this not work well
public function uniformTypeUnion(int bits) returns UniformTypeBitSet {
    return <UniformTypeBitSet>bits;
}

function uniformSubtype(UniformTypeCode code, ProperSubtypeData data) returns SemType {
    return createComplexSemType(0, [[code,data]]);
}

function subtypeData(SemType s, UniformTypeCode code) returns SubtypeData {
    if s is UniformTypeBitSet {
        return (s & (1 << <int>code)) != 0;
    }
    else {
        return getComplexSubtypeData(s, code);
    }
}

public final UniformTypeBitSet NEVER = uniformTypeUnion(0);
public final UniformTypeBitSet NIL = uniformType(UT_NIL);
public final UniformTypeBitSet BOOLEAN = uniformType(UT_BOOLEAN);
public final UniformTypeBitSet INT = uniformType(UT_INT);
public final UniformTypeBitSet FLOAT = uniformType(UT_FLOAT);
public final UniformTypeBitSet DECIMAL = uniformType(UT_DECIMAL);
public final UniformTypeBitSet STRING = uniformType(UT_STRING);
public final UniformTypeBitSet ERROR = uniformType(UT_ERROR);
public final UniformTypeBitSet LIST_RW = uniformType(UT_LIST_RW);
public final UniformTypeBitSet LIST = uniformTypeUnion((1 << UT_LIST_RO) | (1 << UT_LIST_RW));
public final UniformTypeBitSet MAPPING_RW = uniformType(UT_MAPPING_RW);
public final UniformTypeBitSet MAPPING = uniformTypeUnion((1 << UT_MAPPING_RO) | (1 << UT_MAPPING_RW));

// matches all functions
public final UniformTypeBitSet FUNCTION = uniformType(UT_FUNCTION);
public final UniformTypeBitSet TYPEDESC = uniformType(UT_TYPEDESC);
public final UniformTypeBitSet HANDLE = uniformType(UT_HANDLE);

public final UniformTypeBitSet XML = uniformTypeUnion((1 << UT_XML_RO) | (1 << UT_XML_RW));
public final UniformTypeBitSet STREAM = uniformType(UT_STREAM);
public final UniformTypeBitSet FUTURE = uniformType(UT_FUTURE);

// this is SubtypeData|error
public final UniformTypeBitSet TOP = uniformTypeUnion(UT_MASK);
public final UniformTypeBitSet ANY = uniformTypeUnion(UT_MASK & ~(1 << UT_ERROR));
public final UniformTypeBitSet READONLY = uniformTypeUnion(UT_READONLY);
public final UniformTypeBitSet SIMPLE_OR_STRING = uniformTypeUnion((1 << UT_NIL) | (1 << UT_BOOLEAN) | (1 << UT_INT)| (1 << UT_FLOAT)| (1 << UT_DECIMAL)| (1 << UT_STRING));
public final UniformTypeBitSet NUMBER = uniformTypeUnion((1 << UT_INT) | (1 << UT_FLOAT) | (1 << UT_DECIMAL));
public final SemType BYTE = intWidthUnsigned(8);

// Need this type to workaround slalpha4 bug.
// It has to be public to workaround another bug.
public type SubtypePairIterator object {
    public function next() returns record {| [UniformTypeCode, SubtypeData?, SubtypeData?] value; |}?;
};

class SubtypePairIteratorImpl {
    *object:Iterable;
    *SubtypePairIterator;
    private int i1;
    private int i2;
    private final UniformSubtype[] t1;
    private final UniformSubtype[] t2;
    private final UniformTypeBitSet bits;

    function init(SemType t1, SemType t2, UniformTypeBitSet bits) {
        self.i1 = 0;
        self.i2 = 0;
        self.t1 = (t1 is UniformTypeBitSet) ? [] : unpackComplexSemType(t1);
        self.t2 = (t2 is UniformTypeBitSet) ? [] : unpackComplexSemType(t2);
        self.bits = bits;
    }

    public function iterator() returns SubtypePairIterator {
        return self;
    }

    public function next() returns record {| [UniformTypeCode, SubtypeData?, SubtypeData?] value; |}? {
        while true {
            if self.i1 >= self.t1.length() {
                if self.i2 >= self.t2.length() {
                    break;
                }
                var [code, data2] = self.get2();
                self.i2 += 1;
                if self.include(code) {
                    return { value: [code, (), data2] };
                }
            }
            else if self.i2 >= self.t2.length() {
                var [code, data1] = self.get1();
                self.i1 += 1;
                if self.include(code) {
                    return { value: [code, data1, ()] };
                }
            }
            else {
                var [code1, data1] = self.get1();
                var [code2, data2] = self.get2();
                if code1 == code2 {
                    self.i1 += 1;
                    self.i2 += 1;
                    if self.include(code1) {    
                        return { value: [code1, data1, data2] };
                    }
                }
                else if code1 < code2 {
                    self.i1 += 1;
                    if self.include(code1) {
                        return { value: [code1, data1, ()] };
                    }
                }
                else {
                    self.i2 += 1;
                    if self.include(code2) {
                        return { value: [code2, (), data2] };
                    }
                }

            }
        }
        return ();
    } 

    private function include(UniformTypeCode code) returns boolean {
        int c = code;
        return (self.bits & (1 << c)) != 0;
    }

    private function get1() returns UniformSubtype {
        return self.t1[self.i1];
    }

    private function get2() returns UniformSubtype {
        return self.t2[self.i2];
    }
}


public function union(SemType t1, SemType t2) returns SemType {
    UniformTypeBitSet all1;
    UniformTypeBitSet all2;
    UniformTypeBitSet some1;
    UniformTypeBitSet some2;

    if t1 is UniformTypeBitSet {
        if t2 is UniformTypeBitSet {
            return t1|t2;
        }
        else {
            all2 = t2.all;
            some2 = t2.some;
        }
        all1 = t1;
        some1 = 0;
    }
    else {
        all1 = t1.all;
        some1 = t1.some;
        if t2 is UniformTypeBitSet {
            all2 = t2;
            some2 = 0;
        }
        else {
            all2 = t2.all;
            some2 = t2.some;
        }
    }
    
    UniformTypeBitSet all = all1 | all2;
    UniformTypeBitSet some = (some1 | some2) & ~<int>all;
    if some == 0 {
        return uniformTypeUnion(all);
    }
    UniformSubtype[] subtypes = [];
    foreach var [code, data1, data2] in new SubtypePairIteratorImpl(t1, t2, some) {
        SubtypeData data;
        if data1 is () {
            data = <SubtypeData>data2; // if they are both null, something's gone wrong
        }
        else if data2 is () {
            data = data1;
        }
        else {
            var union = ops[code].union;
            data = union(data1, data2);
        }
        if data == true {
            int c = code;
            all |= <UniformTypeBitSet>(1 << c);
        }
        else {
            subtypes.push([code, data]);
        }
    }
    if subtypes.length() == 0 {
        return all;
    }
    return createComplexSemType(all, subtypes);
}

public function intersect(SemType t1, SemType t2) returns SemType {
    UniformTypeBitSet all1;
    UniformTypeBitSet all2;
    UniformTypeBitSet some1;
    UniformTypeBitSet some2;

    if t1 is UniformTypeBitSet {
        if t2 is UniformTypeBitSet {
            return t1&t2;
        }
        else {
            if t1 == 0 {
                return t1;
            }
            if t1 == UT_MASK {
                return t2;
            }
            all2 = t2.all;
            some2 = t2.some;
        }
        all1 = t1;
        some1 = 0;
    }
    else {
        all1 = t1.all;
        some1 = t1.some;
        if t2 is UniformTypeBitSet {
            if t2 == 0 {
                return t2;
            }
            if t2 == UT_MASK {
                return t1;
            }
            all2 = t2;
            some2 = 0;
        }
        else {
            all2 = t2.all;
            some2 = t2.some;
        }
    }
   
    UniformTypeBitSet all = all1 & all2;

    // some(t1 & t2) = some(t1) & some(t2)
    UniformTypeBitSet some = (some1 | all1) & (some2 | all2);

    some &= ~<int>all;
    if some == 0 {
        return uniformTypeUnion(all);
    }
    UniformSubtype[] subtypes = [];
    foreach var [code, data1, data2] in new SubtypePairIteratorImpl(t1, t2, some) {
        SubtypeData data;
        if data1 is () {
            data = <SubtypeData>data2;
        }
        else if data2 is () {
            data = data1;
        }
        else {
            var intersect = ops[code].intersect;
            data = intersect(data1, data2);
        }
        if data != false {
            subtypes.push([code, data]);
        }
    }
    if subtypes.length() == 0 {
        return all;
    }
    return createComplexSemType(all, subtypes);    
}

public function diff(SemType t1, SemType t2) returns SemType {
    UniformTypeBitSet all1;
    UniformTypeBitSet all2;
    UniformTypeBitSet some1;
    UniformTypeBitSet some2;

    if t1 is UniformTypeBitSet {
        if t2 is UniformTypeBitSet {
            return t1 & ~<int>t2;
        }
        else {
            if t1 == 0 {
                return t1;
            }
            all2 = t2.all;
            some2 = t2.some;
        }
        all1 = t1;
        some1 = 0;
    }
    else {
        all1 = t1.all;
        some1 = t1.some;
        if t2 is UniformTypeBitSet {
            if t2 == UT_MASK {
                return <UniformTypeBitSet>0;
            }
            all2 = t2;
            some2 = 0;
        }
        else {
            all2 = t2.all;
            some2 = t2.some;
        }
    }

    // all(t1 \ t2) = all(t1) & not(all(t2)|some(t2))
    UniformTypeBitSet all = all1 & ~<int>(all2 | some2);
    // some(t1 \ t2) = some(t1) & not(all(t2))
    UniformTypeBitSet some = (all1 | some1) & ~<int>all2;
    some &= ~<int>all;

    if some == 0 {
        return uniformTypeUnion(all);
    }
    UniformSubtype[] subtypes = [];
    foreach var [code, data1, data2] in new SubtypePairIteratorImpl(t1, t2, some) {
        SubtypeData data;
        if data1 is () {
            var complement = ops[code].complement;
            data = complement(<SubtypeData>data2);
        }
        else if data2 is () {
            data = data1;
        }
        else {
            var diff = ops[code].diff;
            data = diff(data1, data2);
        }
         if data != false {
            subtypes.push([code, data]);
        }
    }
    if subtypes.length() == 0 {
        return all;
    }
    return createComplexSemType(all, subtypes);        
}

public function complement(SemType t) returns SemType {
    return diff(TOP, t);
}

public function isNever(SemType t) returns boolean {
    return t is UniformTypeBitSet && t == 0;
}

public function isEmpty(TypeCheckContext tc, SemType t) returns boolean {
    if t is UniformTypeBitSet {
        return t == 0;
    }
    else {
        if t.all != 0 {
            // includes all of one or more uniform types
            return false;
        }
        foreach var st in unpackComplexSemType(t) {
            var [code, data] = st;
            var isEmpty = ops[code].isEmpty;
            if !isEmpty(tc, data) {
                return false;
            }
        }
        return true;
    }
}
    
public function isSubtype(TypeCheckContext tc, SemType t1, SemType t2) returns boolean { 
    return isEmpty(tc, diff(t1, t2));
}

public function isSubtypeSimple(SemType t1, UniformTypeBitSet t2) returns boolean {
    int bits;
    if t1 is UniformTypeBitSet {
        bits = t1;
    }
    else {
        bits = t1.all | t1.some;
    }
    return (bits & ~<int>t2) == 0;
}

// If t is a non-empty subtype of a built-in unsigned int subtype (Unsigned8/16/32),
// then return the smallest such subtype. Otherwise, return t.
public function widenUnsigned(SemType t) returns SemType {
    if t is UniformTypeBitSet {
        return t;
    }
    else {
        if !isSubtypeSimple(t, INT) {
            return t;
        }
        SubtypeData data = intSubtypeWidenUnsigned(subtypeData(t, UT_INT));
        if data is boolean {
            return INT;
        }
        else {
            return uniformSubtype(UT_INT, data);
        }
    }
}

// This is a temporary API that identifies when a SemType corresponds to a type T[]
// where T is a union of complete basic types.
public function simpleArrayMemberType(Env env, SemType t) returns UniformTypeBitSet? {
    if t is UniformTypeBitSet {
        return t == LIST ? TOP : ();
    }
    else {
        if !isSubtypeSimple(t, LIST) {
            return ();
        }
        Bdd[] bdds = [<Bdd>getComplexSubtypeData(t, UT_LIST_RO), <Bdd>getComplexSubtypeData(t, UT_LIST_RW)];
        UniformTypeBitSet[] memberTypes = [];
        foreach var bdd in bdds {
            if bdd is boolean {
                if bdd {
                    memberTypes.push(TOP);
                }
                else {
                    return ();
                }
            }
            else {
                if bdd.left != true || bdd.right != false || bdd.right != false {
                    return ();
                }
                ListAtomicType atomic = env.listAtomType(bdd.atom);
                if atomic.members.length() > 0 {
                    return ();
                }
                SemType memberType = atomic.rest;
                if memberType is UniformTypeBitSet {
                    memberTypes.push(memberType);
                }
                else {
                    return ();
                }
            }
        }
        if memberTypes[0] != (memberTypes[1] & UT_READONLY) {
            return ();
        }
        return memberTypes[1];
    }
}

// This is a temporary API that identifies when a SemType corresponds to a type T[]
// where T is a union of complete basic types.
public function simpleMapMemberType(Env env, SemType t) returns UniformTypeBitSet? {
    if t is UniformTypeBitSet {
        return t == MAPPING ? TOP : ();
    }
    else {
        if !isSubtypeSimple(t, MAPPING) {
            return ();
        }
        Bdd[] bdds = [<Bdd>getComplexSubtypeData(t, UT_MAPPING_RO), <Bdd>getComplexSubtypeData(t, UT_MAPPING_RW)];
        UniformTypeBitSet[] memberTypes = [];
        foreach var bdd in bdds {
            if bdd is boolean {
                if bdd {
                    memberTypes.push(TOP);
                }
                else {
                    return ();
                }
            }
            else {
                if bdd.left != true || bdd.right != false || bdd.right != false {
                    return ();
                }
                MappingAtomicType atomic = env.mappingAtomType(bdd.atom);
                if atomic.names.length() > 0 {
                    return ();
                }
                SemType memberType = atomic.rest;
                if memberType is UniformTypeBitSet {
                    memberTypes.push(memberType);
                }
                else {
                    return ();
                }
            }
        }
        if memberTypes[0] != (memberTypes[1] & UT_READONLY) {
            return ();
        }
        return memberTypes[1];
    }
}

public type Value readonly & record {|
    string|int|float|boolean|() value;
|};

// If the type contains exactly onr shape, return a value
// having that shape.
public function singleShape(SemType t) returns Value? {
    if t === NIL {
        return { value: () };
    }
    else if t is UniformTypeBitSet {
        return ();
    }
    else if isSubtypeSimple(t, INT) {
        SubtypeData sd = getComplexSubtypeData(t, UT_INT);
        int? value = intSubtypeSingleValue(sd);
        return value == () ? () : { value };
    }
    else if isSubtypeSimple(t, FLOAT) {
        SubtypeData sd = getComplexSubtypeData(t, UT_FLOAT);
        float? value = floatSubtypeSingleValue(sd);
        return value == () ? () : { value };
    }
    else if isSubtypeSimple(t, STRING) {
        SubtypeData sd = getComplexSubtypeData(t, UT_STRING);
        string? value = stringSubtypeSingleValue(sd);
        return value == () ? () : { value };
    }
    else if isSubtypeSimple(t, BOOLEAN) {
        SubtypeData sd = getComplexSubtypeData(t, UT_BOOLEAN);
        boolean? value = booleanSubtypeSingleValue(sd);
        return value == () ? () : { value };
    }
    return ();
}

public function singleton(string|int|float|boolean|() v) returns SemType {
    if v is () {
        return NIL;
    }
    else if v is int {
        return intConst(v);
    }
    else if v is float {
        return floatConst(v);
    }
    else if v is string {
        return stringConst(v);
    }
    else {
        return booleanConst(v);
    }
}

public function isReadOnly(SemType t) returns boolean {
    UniformTypeBitSet bits;
    if t is UniformTypeBitSet {
        bits = t;
    }
    else {
        bits = t.all | t.some;
    }
    return (bits & UT_RW_MASK) == 0;
}

public function constUniformTypeCode(string|int|float|boolean|() v) returns UT_STRING|UT_INT|UT_FLOAT|UT_BOOLEAN|UT_NIL {
    if v is () {
        return UT_NIL;
    }
    else if v is int {
        return UT_INT;
    }
    else if v is float {
        return UT_FLOAT;
    }
    else if v is string {
        return UT_STRING;
    }
    else {
        return UT_BOOLEAN;
    }
}

public function constBasicType(string|int|float|boolean|() v) returns UniformTypeBitSet {
    return  uniformType(constUniformTypeCode(v));
}

public function containsConst(SemType t, string|int|float|boolean|() v) returns boolean {
    if v is () {
        return containsNil(t);
    }
    else if v is int {
        return containsConstInt(t, v);
    }
    else if v is float {
        return containsConstFloat(t, v);
    }
    else if v is string {
        return containsConstString(t, v);
    }
    else {
        return containsConstBoolean(t, v);
    }
}

public function containsNil(SemType t) returns boolean {
    if t is UniformTypeBitSet {
        return (t & (1 << UT_NIL)) != 0;
    }
    else {
        return <boolean>getComplexSubtypeData(t, UT_NIL);
    }
}


public function containsConstString(SemType t, string s) returns boolean {
    if t is UniformTypeBitSet {
        return (t & (1 << UT_STRING)) != 0;
    }
    else {
        return stringSubtypeContains(getComplexSubtypeData(t, UT_STRING), s);
    }
}

public function containsConstInt(SemType t, int n) returns boolean {
    if t is UniformTypeBitSet {
        return (t & (1 << UT_INT)) != 0;
    }
    else {
        return intSubtypeContains(getComplexSubtypeData(t, UT_INT), n);
    }
}

public function containsConstFloat(SemType t, float n) returns boolean {
    if t is UniformTypeBitSet {
        return (t & (1 << UT_FLOAT)) != 0;
    }
    else {
        return floatSubtypeContains(getComplexSubtypeData(t, UT_FLOAT), n);
    }
}

public function containsConstBoolean(SemType t, boolean b) returns boolean {
    if t is UniformTypeBitSet {
        return (t & (1 << UT_BOOLEAN)) != 0;
    }
    else {
        return booleanSubtypeContains(getComplexSubtypeData(t, UT_BOOLEAN), b);
    }
}

public function singleNumericType(SemType semType) returns UniformTypeBitSet? {
    SemType numType = intersect(semType, NUMBER);
    if numType == NEVER {
        return ();
    }
    if isSubtypeSimple(numType, INT) {
        return INT;
    }
    if isSubtypeSimple(numType, FLOAT) {
        return FLOAT;
    }
    if isSubtypeSimple(numType, DECIMAL) {
        return DECIMAL;
    }
    return ();
}

public function typeCheckContext(Env env) returns TypeCheckContext {
    return new(env);
}

public function createJson(Env env) returns SemType {
    ListDefinition listDef = new;
    MappingDefinition mapDef = new;
    SemType j = union(SIMPLE_OR_STRING, union(listDef.getSemType(env), mapDef.getSemType(env)));
    _ = listDef.define(env, [], j);
    _ = mapDef.define(env, [], j);
    return j;
}

final readonly & UniformTypeOps[] ops;

function init() {
    ops = [
        {}, // nil
        booleanOps, // boolean
        listRoOps, // RO list
        mappingRoOps, // RO mapping
        {}, // RO table
        {}, // RO xml
        {}, // RO object
        intOps, // int
        floatOps, // float
        {}, // decimal
        stringOps, // string
        errorOps, // error
        functionOps,  // function
        {}, // typedesc
        {}, // handle
        {}, // unused
        {}, // RW future
        {}, // RW stream
        listRwOps, // RW list
        mappingRwOps, // RW mapping
        {}, // RW table
        {}, // RW xml
        {} // RW object
   ];
}
