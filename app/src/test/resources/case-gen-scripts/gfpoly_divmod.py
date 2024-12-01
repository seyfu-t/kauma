import argparse
import base64
import os
import json
import random

# Function to generate a random 16-byte block
def generate_block():
    return base64.b64encode(os.urandom(16)).decode('utf-8')

# Function to generate the test cases for gfpoly_divmod
def generate_gfpoly_divmod_testcases(num_cases):
    testcases = {}
    for i in range(1, num_cases + 1):
        # Generate a list of random blocks for A (must be at least 2 elements)
        A = [generate_block() for _ in range(random.randint(2, 10))]  # Random size between 2 and 5 elements
        # Generate a list of random blocks for B (must have fewer elements than A)
        B = [generate_block() for _ in range(random.randint(1, len(A)-1))]  # Fewer elements than A

        # Ensure that the condition b < a is satisfied by having len(B) < len(A)
        testcases[f"gfpoly_divmod{i}"] = {
            "action": "gfpoly_divmod",
            "arguments": {
                "A": A,
                "B": B
            }
        }
    
    return testcases

# Main function
def main():
    parser = argparse.ArgumentParser(description="Generate test cases for gfpoly_divmod")
    parser.add_argument("num_cases", nargs="?", type=int, default=100, help="Number of test cases to generate")
    args = parser.parse_args()

    # Generate the test cases
    testcases = generate_gfpoly_divmod_testcases(args.num_cases)

    # Create the JSON structure
    data = {"testcases": testcases}

    # Save to a minified JSON file
    filename = f"gfpoly_divmod_{args.num_cases}.json"
    with open(filename, 'w') as f:
        json.dump(data, f)  # No indent, this minifies the JSON
    print(f"Test cases saved to {filename}")

if __name__ == "__main__":
    main()
