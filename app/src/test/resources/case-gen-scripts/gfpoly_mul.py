import base64
import random
import json
import argparse

def generate_random_base64():
    """Generate a random 16-byte base64-encoded string."""
    random_bytes = random.randbytes(16)
    return base64.b64encode(random_bytes).decode('ascii')

def generate_mul_test_case(identifier):
    """Generate a single multiplication test case with a unique identifier."""
    # Randomize the number of coefficients (1 to 10 for variety)
    num_coeff_a = random.randint(1, 10)
    num_coeff_b = random.randint(1, 10)
    
    # Generate random coefficients for A and B
    coeff_a = [generate_random_base64() for _ in range(num_coeff_a)]
    coeff_b = [generate_random_base64() for _ in range(num_coeff_b)]
    
    return {
        f"gfpoly_mul{identifier}": {
            "action": "gfpoly_mul",
            "arguments": {
                "A": coeff_a,
                "B": coeff_b
            }
        }
    }

def generate_mul_test_cases(total_cases=100):
    """Generate a specified number of multiplication test cases."""
    testcases = {}
    for i in range(total_cases):
        testcases.update(generate_mul_test_case(i))
    return {"testcases": testcases}

def main():
    # Set up argument parser
    parser = argparse.ArgumentParser(description="Generate GF Polynomial Multiplication Test Cases")
    parser.add_argument(
        "num_cases", 
        type=int, 
        nargs="?", 
        default=100, 
        help="Number of test cases to generate (default is 100)"
    )
    args = parser.parse_args()

    # Generate the test cases
    testcases = generate_mul_test_cases(args.num_cases)

    # Save to a JSON file
    output_file = f"gfpoly_mul_{args.num_cases}.json"
    with open(output_file, "w") as f:
        json.dump(testcases, f, indent=4)

    print(f"Multiplication test cases saved to {output_file}")

if __name__ == "__main__":
    main()
