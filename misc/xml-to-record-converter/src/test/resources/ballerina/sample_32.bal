type Bk_Chapter record {
    int[] number;
};

@xmldata:Name {value: "book"}
type Bk_Book record {
    string[] title;
    string subtitle;
    string language;
    Bk_Chapter[] chapter;
};
