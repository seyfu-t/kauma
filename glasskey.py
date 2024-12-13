import base64
import hashlib
import hmac

def long_to_bytes_little_endian(value):
    return value.to_bytes(8, byteorder='little', signed=False)

def swap_byte_order(data):
    return data[::-1]

def sha256(data):
    return hashlib.sha256(data).digest()

def hmac_sha256(data, key):
    return hmac.new(key, data, hashlib.sha256).digest()

def prng_single(key, seed, num):
    num_star = long_to_bytes_little_endian(num)

    key_sha = sha256(key)
    seed_sha = sha256(seed)

    key_star = key_sha[:16] + seed_sha[:16]

    hmac_result = hmac_sha256(num_star, key_star)

    print(f"KEY: {key_sha.hex()}")
    print(f"SEED: {seed_sha.hex()}")
    print(f"HMAC: {hmac_result.hex()}")

    return hmac_result


def prng_all(key, seed, nums):
    result = []

    for num in nums:
        block = prng_single(key, seed, num)[:num]
        encoded = base64.b64encode(block).decode('utf-8')
        result.append(encoded)

    return result

# Example usage:
# key = b'some_key'
# seed = b'some_seed'
# nums = [32, 64, 128]
# prng_all(key, seed, nums)


key = b'FB5550B1DFF89F250CFBBCC98E9632ED98C6A3D23CFB7BF01D7C0F426FC0A61'
seed = b'82247ABB3C879AA8E5BF0A9F1942552213640A4A0A8CC1AE094FB4D2C5742A0'

hmac_result = prng_single(key,seed,4)

key_b64 = "T01HV1RG"
seed_b64 = "ur1EoxDElJs="

key = base64.b64decode(key_b64)
seed = base64.b64decode(seed_b64)

hmac_result = prng_all(key, seed, [4])

print(hmac_result)