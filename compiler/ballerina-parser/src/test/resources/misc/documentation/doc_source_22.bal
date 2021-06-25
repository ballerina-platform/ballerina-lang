# Scenario 1A
# Code block should start with backticks ```bal
# dummy code line
#
# dummy code line
# ```
function foo() {
}

# Scenario 1B
# Code block should start with backticks ```bal
# dummy code line
#
# dummy code line
# ```
# Doc line after the erroneous block
function foo() {
}

# Scenario 2A
# Code block should start with backticks ```bal
# dummy code line
# dummy code line
# ```Cannot have anything inline with code ref end backtick
function foo() {
}

# Scenario 2B
# Code block should start with backticks ```bal
# dummy code line
# dummy code line
# ```Cannot have anything inline with code ref end backtick
# Doc line after the erroneous block
function foo() {
}

# Scenario 3A
# ```bal
# dummy code line
# dummy code line
# ```Cannot have anything inline with code ref end backtick
function foo() {
}

# Scenario 3B
# ```bal
# dummy code line
# dummy code line
# ```Cannot have anything inline with code ref end backtick
# Doc line after the erroneous block
function foo() {
}
