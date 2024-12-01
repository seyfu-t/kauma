import argparse
import base64
import json
import random
import os

# Function to generate a random 16-byte block
def generate_block():
    return base64.b64encode(os.urandom(16)).decode('utf-8')

# Function to generate the test cases
def generate_testcases(num_cases):
    testcases = {}
    for i in range(1, num_cases + 1):
        # Alternate between "xex" and "gcm"
        semantic = "xex" if i % 2 == 1 else "gcm"
        
        # Generate the block (16 bytes, base64 encoded)
        block = generate_block()
        
        testcases[f"block2poly{i}"] = {
            "action": "block2poly",
            "arguments": {
                "semantic": semantic,
                "block": block
            }
        }
    return testcases

# Main function
def main():
    parser = argparse.ArgumentParser(description="Generate test cases for block2poly")
    parser.add_argument("num_cases", nargs="?", type=int, default=100, help="Number of test cases to generate")
    args = parser.parse_args()

    # Generate the test cases
    testcases = generate_testcases(args.num_cases)

    # Create the JSON structure
    data = {"testcases": testcases}

    # Save to a JSON file
    filename = f"block2poly_{args.num_cases}.json"
    with open(filename, 'w') as f:
        json.dump(data, f)
    print(f"Test cases saved to {filename}")

if __name__ == "__main__":
    main()
