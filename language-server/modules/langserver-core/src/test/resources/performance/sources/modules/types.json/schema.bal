// JSON representation of types

public type Type Nil|Boolean|Int|Float|Decimal|String|Error|Typedesc|Handle|Xml|Json|Never|Any|ReadOnly
    |Byte|BuiltinIntSubtype|Single|Union|Intersection|Tuple|Record|Function|Rec|Ref;

public const Nil = "nil";
public const Boolean = "boolean";
public const Int = "int";
public const Float = "float";
public const Decimal = "decimal";
public const String = "string";
public const Typedesc = "typedesc";
public const Handle = "handle";
public const Xml = "xml";
public const Json = "json";
public const Never = "never";
public const Any = "any";
public const ReadOnly = "readonly";

public const Byte = "byte";
public type BuiltinIntSubtype "int32"|"int16"|"int8"|"uint32"|"uint16"|"uint8";

public type Single SingleString|SingleInt|SingleBoolean;
public type SingleString ["string", string];
// Allow string here, so it can work with JSON parsers that represent numbers using doubles
public type SingleInt ["int", int|string];

public type SingleBoolean boolean;

public type Union ["|", Type...];
public type Intersection ["&", Type...];

public type Tuple ["tuple", Type...];

// Last Type if present is interpreted as the tuple rest type. So
// `["list"]` means any list
// `["list", "int"]` means a list of any length containing only integers
// `["list", "string", "int"]` means a list whose first member is a string
// and whose other members are integers
// Note that `["list", "int", "boolean", "never"]`
// is the same as `["tuple", "int", "boolean"]`
public type List ["list", Type...];
public type Record ["record", Field...];
public type Field [string, Type];

// This should be `Type`, not `Type...` but slalpha5 does not allow it.

public type Error "error" | ["error", Type...];
// `["function" "string", "boolean", "int"]` means any function of two arguments, string and boolean, returning an integer
// `["function" "int"]` means any function of zero arguments returning an integer
// `["function"]` means any function
public type Function ["function", Type...];
// This should be `Type`, not `Type...` but slalpha5 does not allow it.
public type Rec ["rec", string, Type...];
public type Ref ["ref", string];

