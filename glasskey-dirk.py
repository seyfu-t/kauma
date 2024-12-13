import base64
import hashlib
import hmac

def glasskey_prng_block(K: bytes, s: bytes, i: int) -> bytes:
    # i in 8-Byte Little-Endian konvertieren
    i_bytes = i.to_bytes(8, byteorder='little')
    
    # K* = SHA256(K) || SHA256(s)
    K_hash = hashlib.sha256(K).digest()
    s_hash = hashlib.sha256(s).digest()
    print(K_hash.hex())
    print(s_hash.hex())
    K_star = K_hash + s_hash
    
    # HMAC-SHA256(i_bytes, key=K_star)
    return hmac.new(K_star, i_bytes, hashlib.sha256).digest()

def glasskey_prng_generate(K: bytes, s: bytes, total_bytes: int) -> bytes:
    """
    Erzeugt einen kontinuierlichen Byte-Strom aus dem PRNG.
    total_bytes: Gesamtanzahl der benötigten Bytes
    """
    # Anzahl benötigter 32-Byte-Blöcke
    block_count = (total_bytes + 31) // 32
    stream = b""
    for i in range(block_count):
        stream += glasskey_prng_block(K, s, i)
    return stream[:total_bytes]

def glasskey_prng_int_bits_action(args: dict) -> dict:
    # agency_key als ASCII interpretieren
    agency_key = base64.b64decode(args["agency_key"])
    # seed base64-dekodieren
    seed = base64.b64decode(args["seed"])
    bit_lengths = args["bit_lengths"]

    # Berechne, wie viele Bytes insgesamt benötigt werden
    # Für jeden Wert: l = ceil(b/8)
    # Summiere alle l auf, um den gesamten Bytebedarf im Vorhinein zu bestimmen.
    total_bytes_needed = 0
    for b in bit_lengths:
        l = (b + 7) // 8  # ceil(b/8)
        total_bytes_needed += l

    # Erzeuge einen kontinuierlichen Byte-Strom
    prng_stream = glasskey_prng_generate(agency_key, seed, total_bytes_needed)

    results = []
    offset = 0
    for b in bit_lengths:
        l = (b + 7) // 8
        chunk = prng_stream[offset:offset+l]
        offset += l

        # Interpretieren als Little-Endian Integer
        # int.from_bytes(..., 'little') liest Little-Endian
        num = int.from_bytes(chunk, 'little')

        # Nur die untersten b Bits extrahieren
        mask = (1 << b) - 1
        num = num & mask

        results.append(num)

    return {"ints": results}

def glasskey_prng_action(args: dict) -> dict:
    # agency_key als ASCII interpretieren (kein Base64-Decoding)
    agency_key = base64.b64decode(args["agency_key"])
    # seed Base64-dekodieren
    seed = base64.b64decode(args["seed"])
    lengths = args["lengths"]

    # Alle benötigten Blöcke generieren
    stream = b""
    for i in range(len(lengths)-1):
        block = glasskey_prng_block(agency_key, seed, i)
        print(block.hex())
        stream += block

    # print(stream.hex())
    
    # Nun aus dem generierten Strom die gewünschten Längen entnehmen
    results = []
    offset = 0
    for length in lengths:
        chunk = stream[offset:offset+length]
        offset += length
        # Base64 enkodieren
        results.append(base64.b64encode(chunk).decode('utf-8'))
    
    return {"blocks": results}


value = {
    "agency_key": "T01HV1RG",
    "seed": "ur1EoxDElJs=",
    "lengths": [
        4,
        8,
        13,
        12,
        1,
        9
    ]
}

glasskey_prng_action(value)