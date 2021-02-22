type Grade record {
    string name;
}

School school = getSchool();

School school2 = let Grade grade = {name:"Grade 1"} in createSchool([grade]);