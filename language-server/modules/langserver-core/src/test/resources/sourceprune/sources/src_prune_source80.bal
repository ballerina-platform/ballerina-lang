function testServiceAnnotAccess() returns boolean {
    i
    Annot? annot = <Annot?> reflect:getServiceAnnotations(ser, "v4");
}