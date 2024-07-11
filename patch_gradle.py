import os

# Function to find and replace text in a file
def find_and_replace_in_file(file_path, search_text, replace_text):
    with open(file_path, 'r') as file:
        file_data = file.read()

    if search_text in file_data:
        file_data = file_data.replace(search_text, replace_text)
        with open(file_path, 'w') as file:
            file.write(file_data)
        print(f"Replaced text in {file_path}")
    else:
        print(f"No matching text found in {file_path}")

# Function to search for build.gradle files and replace text
def search_and_replace(start_dir, search_text, replace_text):
    for root, dirs, files in os.walk(start_dir):
        for file in files:
            if file == 'build.gradle':
                file_path = os.path.join(root, file)
                find_and_replace_in_file(file_path, search_text, replace_text)

# Define the search and replace text
search_text = "    jvmArgs = ['--add-opens=java.base/java.util=ALL-UNNAMED']"
replace_text = "    jvmArgs = ['--add-opens=java.base/java.util=ALL-UNNAMED', '-XX:+HeapDumpOnOutOfMemoryError']"

# Start the search and replace in the current directory
search_and_replace('.', search_text, replace_text)
