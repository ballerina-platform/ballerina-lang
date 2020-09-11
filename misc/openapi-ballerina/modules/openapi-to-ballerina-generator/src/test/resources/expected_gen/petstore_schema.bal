
type Pet record {
     int id;
     string name;
     string tag?;
     string 'type?;
};

type Dog record {
    *Pet;
     boolean bark?;
};

type Error record {
     int code;
     string message;
};
