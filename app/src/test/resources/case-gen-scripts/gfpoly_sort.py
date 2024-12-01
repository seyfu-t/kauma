import base64
import random
import json
import sys

def generate_random_base64():
    """Generate a random 16-byte Base64-encoded string."""
    random_bytes = random.randbytes(16)
    return base64.b64encode(random_bytes).decode('ascii')

def generate_test_case(max_degree, num_polynoms):
    """
    Generate a test case for gfpoly_sort.

    Args:
    - max_degree: Maximum degree of each polynomial (max number of coefficients).
    - num_polynoms: Number of polynomials to generate.
    """
    polys = []

    for _ in range(num_polynoms):
        # Each polynomial can have up to `max_degree + 1` coefficients (degree + constant term)
        poly = [generate_random_base64() for _ in range(random.randint(1, max_degree + 1))]
        polys.append(poly)

    test_case = {
        "testcases": {
            "gfpoly_sort1": {
                "action": "gfpoly_sort",
                "arguments": {
                    "polys": polys
                }
            }
        }
    }

    # Save to a file or return as JSON
    output_file = "gfpoly_sort_generated.json"
    with open(output_file, "w") as f:
        json.dump(test_case, f)

    print(f"Test case saved to {output_file}")

# Main execution
if __name__ == "__main__":
    if len(sys.argv) != 3:
        print("Usage: python script.py <max_degree> <num_polynoms>")
        sys.exit(1)

    try:
        max_degree = int(sys.argv[1])
        num_polynoms = int(sys.argv[2])

        if max_degree < 0 or num_polynoms <= 0:
            raise ValueError

        generate_test_case(max_degree, num_polynoms)

    except ValueError:
        print("Both arguments must be positive integers.")
        sys.exit(1)
