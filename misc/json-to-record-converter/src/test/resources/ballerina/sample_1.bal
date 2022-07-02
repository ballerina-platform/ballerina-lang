type SportsItem record {
    string question;
    (int|string|(int|string)[])[] options;
    string answer;
};

type MathsItem record {
    string question;
    (decimal|int|string)[] options;
    string answer;
};

type NewRecord record {
    SportsItem[] sports;
    MathsItem[] maths;
};
