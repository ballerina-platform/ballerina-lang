public type Employee record {
	readonly string username;
	int id;
};

public type UserTable table<Employee> key(username);

