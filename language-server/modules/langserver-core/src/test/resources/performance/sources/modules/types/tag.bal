// Inherently immutable
public const UT_NIL        = 0x00;
public const UT_BOOLEAN    = 0x01;

// Selectively immutable; immutable half
public const UT_LIST_RO    = 0x02;
public const UT_MAPPING_RO = 0x03;
public const UT_TABLE_RO   = 0x04;
public const UT_XML_RO     = 0x05;
public const UT_OBJECT_RO  = 0x06;

// Rest of inherently immutable
public const UT_INT        = 0x07;
public const UT_FLOAT      = 0x08;
public const UT_DECIMAL    = 0x09;
public const UT_STRING     = 0x0A;
public const UT_ERROR      = 0x0B;
public const UT_FUNCTION   = 0x0C;
public const UT_TYPEDESC   = 0x0D;
public const UT_HANDLE     = 0x0E;

// Inherently mutable
public const UT_FUTURE     = 0x10;
public const UT_STREAM     = 0x11;

// Selectively immutable; mutable half
public const UT_LIST_RW    = 0x12;
public const UT_MAPPING_RW = 0x13;
public const UT_TABLE_RW   = 0x14;
public const UT_XML_RW     = 0x15;
public const UT_OBJECT_RW  = 0x16;

