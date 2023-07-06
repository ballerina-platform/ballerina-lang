function foo() {
    map<int> marks = {sam: 50, jon: 60, sam: 50, jon: 60, sam: 50,    jon: 60};

    Humanities humanitiesMarks = {
        history: 80, geography: 75};

    Humanities humanitiesMarks = {history: 80,
geography: 75};

    map<int> allMarks = {physics: 100, ...humanitiesMarks, chemistry: 75
    };
}
