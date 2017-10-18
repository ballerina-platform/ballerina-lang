import lang.annotations.doc;

@doc:Description{value:789}
typemapper customTypeMapper (int length) (boolean) {
    if (length > 10) {
        return true;
    } else {
        return false;
    }
}
