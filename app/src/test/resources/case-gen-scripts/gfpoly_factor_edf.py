import argparse
import base64
import os
import json
import random

# Function to generate a random 16-byte block
def generate_block():
    return base64.b64encode(os.urandom(16)).decode('utf-8')

# Function to generate the test cases for gfpoly_factor_edf
def generate_gfpoly_factor_edf_testcases(num_cases):
    testcases = {}
    for i in range(1, num_cases + 1):
        # Generate the list F with random blocks, ensuring the last element is "gAAAAAAAAAAAAAAAAAAAAA=="
        F = [generate_block() for _ in range(random.randint(3, 8))]  # Random size between 3 and 8 elements
        F.append("gAAAAAAAAAAAAAAAAAAAAA==")  # Ensure last element is always "gAAAAAAAAAAAAAAAAAAAAA=="
        
        # Generate d, which is a value between 1 and len(F)-1
        d = random.randint(1, len(F) - 1)

        # Create a test case with a unique name
        testcases[f"gfpoly_factor_edf{i}"] = {
            "action": "gfpoly_factor_edf",
            "arguments": {
                "F": F,
                "d": d
            }
        }
    
    return testcases

# Main function
def main():
    parser = argparse.ArgumentParser(description="Generate test cases for gfpoly_factor_edf")
    parser.add_argument("num_cases", nargs="?", type=int, default=100, help="Number of test cases to generate")
    args = parser.parse_args()

    # Generate the test cases
    testcases = generate_gfpoly_factor_edf_testcases(args.num_cases)

    # Create the JSON structure
    data = {"testcases": testcases}

    # Save to a minified JSON file
    filename = f"gfpoly_factor_edf_{args.num_cases}.json"
    with open(filename, 'w') as f:
        json.dump(data, f)  # No indent, this minifies the JSON
    print(f"Test cases saved to {filename}")

if __name__ == "__main__":
    main()
