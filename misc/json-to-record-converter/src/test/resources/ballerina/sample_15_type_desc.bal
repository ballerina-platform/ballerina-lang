type NewRecord record {
    string id;
    record {
        string name;
        (record {
            string id?;
            string description?;
            string cid?;
        }|string) value;
    }[] characteristic;
};
