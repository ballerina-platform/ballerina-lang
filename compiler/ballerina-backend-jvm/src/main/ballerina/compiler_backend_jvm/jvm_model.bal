public type JarFile record {
    map<string> manifestEntries;
    map<byte[]> jarEntries;
};
