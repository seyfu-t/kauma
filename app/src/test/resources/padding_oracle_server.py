from cryptography.hazmat.primitives import padding
import socket
import threading
import base64

class ServerSimulator:
    def __init__(self, demo_key=b'\x01\x02\x03\x04\x05\x06\x07\x08\x09\x0a\x0b\x0c\x0d\x0e\x0f\x10'):
        self.demo_key = demo_key
        self.initial_ciphertext = b''
        self.excepted_q_blocks = 0
        self.q_blocks = []

    def xor_buf(self, block1, block2):
        return bytes(a ^ b for a, b in zip(block1, block2))

    def slice_blocks_16(self, buffer):
        blocks = []
        for i in range(0, len(buffer), 16):
            blocks.append(buffer[i:i+16])
        return blocks

    def encrypt(self, plain):
        unpadder = padding.PKCS7(128).padder()
        plain = unpadder.update(plain) + unpadder.finalize()
    
        blocks = self.slice_blocks_16(plain)
        ciphertext = b''
        prev_block = b'\x00' * 16

        for block in blocks:
            block = self.xor_buf(block1=block, block2=prev_block)
            block = self.xor_buf(block, self.demo_key)
            prev_block = block
            ciphertext += block

        return ciphertext

    def create_padding_oracle_response(self):
        response = b''
        for q_block in self.q_blocks:
            plain = self.xor_buf(self.initial_ciphertext, self.demo_key)
            plain = self.xor_buf(plain, q_block)
            try:
                unpadder = padding.PKCS7(128).unpadder()
                unpadder.update(plain)
                unpadder.finalize()
            except ValueError:
                response += b'\x00'
            else:
                response += b'\x01'
        return response

    def add_q_block(self, block):
        self.q_blocks.append(block)
        self.excepted_q_blocks -= 1
        if self.excepted_q_blocks == 0:
            response = self.create_padding_oracle_response()
            self.q_blocks.clear()
            return response

    def recv_all(self, conn, length):
        data = b''
        while len(data) < length:
            packet = conn.recv(length - len(data))
            if not packet:
                return None
            data += packet
        return data

    def start_server(self, host='127.0.0.1', port=1337):
        with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
            server_socket.bind((host, port))
            server_socket.listen(1)
            print(f"Server listening on {host}:{port}")

            while True:
                conn, addr = server_socket.accept()
                with conn:
                    #print("Connected " + str(addr))
                    self.initial_ciphertext = self.recv_all(conn, 16)

                    while True:
                        q_count_data = self.recv_all(conn, 2)
                        if not q_count_data:
                            break
                        self.excepted_q_blocks = int.from_bytes(q_count_data, byteorder='little')
                        #print("Q blocks expected received:", self.excepted_q_blocks)
                        
                        if self.excepted_q_blocks == 0:
                            break

                        total_bytes_expected = self.excepted_q_blocks * 16
                        bytes_received = self.recv_all(conn, total_bytes_expected)
                        if not bytes_received:
                            break
                        #print("bytes_received received:", bytes_received)
                       
                        for i in range(0, total_bytes_expected, 16):
                            q_block = bytes_received[i:i + 16]
                            #print("q_block received:", q_block)
                            response = self.add_q_block(q_block)
                            
                            if response:
                                #print("response:", response)
                                conn.sendall(response)


server = ServerSimulator() 
server_thread = threading.Thread(target=server.start_server)
text = b'HelloWorld'
encrypted = server.encrypt(text)
print(encrypted)
#print(ServerSimulator.encrypt(server, 0xAAAAAAAAAAAAAAAA))
server_thread.start()