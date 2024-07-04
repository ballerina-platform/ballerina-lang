import re

# Define the JVM arguments for heap dump on OutOfMemory exception
heap_dump_args = '-XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=./heapdump.hprof'

# File path to build.gradle (assuming it's in the current directory)
file_path = 'build.gradle'

# Read the build.gradle file
with open(file_path, 'r') as file:
    build_gradle_content = file.read()

# Define the regex pattern to find the build task and its contents
build_task_pattern = r'task\s+?\b(build)\b\s*?\{([^{}]*(\{[^{}]*\}[^{}]*)*)*\s*?\}'

# Find the build task in the file content
match = re.search(build_task_pattern, build_gradle_content, re.DOTALL)

if match:
    # Get the build task content
    build_task_content = match.group(0)
    
    # Check if jvmArgs is already present in the build task
    if 'jvmArgs' not in build_task_content:
        # Append JVM args to the build task content
        modified_build_task_content = re.sub(r'\}', f'    jvmArgs "{heap_dump_args}"\n}}', build_task_content, count=1)
        
        # Replace the old build task content with the modified one
        updated_content = build_gradle_content[:match.start()] + modified_build_task_content + build_gradle_content[match.end():]
        
        # Write the updated content back to the build.gradle file
        with open(file_path, 'w') as file:
            file.write(updated_content)
        
        print(f"Successfully added JVM args for heap dump to the build task in {file_path}")
    else:
        print("JVM args already present in the build task.")
else:
    print("Build task not found in build.gradle file")
