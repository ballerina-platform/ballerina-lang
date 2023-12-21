type ZASN_SDTXP record {
    string TDFORMAT?;
    string TDLINE;
    @xmldata:Attribute
    string SEGMENT;
};

type ZASN_SDTX record {
    string TDID;
    string TDSPRAS;
    ZASN_SDTXP[] ZASN_SDTXP;
    @xmldata:Attribute
    string SEGMENT;
};

type ROOT record {
    ZASN_SDTX[] ZASN_SDTX;
};
