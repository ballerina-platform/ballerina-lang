public type E1 distinct error;
public type E2 distinct (E1 & error<record {|anydata body;|}>);
