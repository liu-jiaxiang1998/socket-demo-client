# -*- coding: utf-8 -*-
# import sys
#
# if __name__ == "__main__":
#     # 获取输入的参数
#     args = sys.argv[1:]
#
#     # 打印输入的参数
#     print("输入的参数是：", args)

import socket
import json

result_frame = [-1, 123, 456, 789]
data_to_send = json.dumps(result_frame)

host = 'localhost'
port = 8999

client_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
client_socket.connect((host, port))

# 接收 Java 程序发送的 JSON 数据
received_json = client_socket.recv(1024).decode('utf-8')

# 解析 JSON 数据
received_array = json.loads(received_json)

# 输出解析结果
print("Received array from Java:", received_array)
# print(type(received_array[0]))
# print(type(received_array[1]))
# print(type(received_array[2]))
# print(type(received_array[3]))
# print(type(received_array[4]))
a,b,c,d,e = received_array
print(type(a))
print(type(b))
print(type(c))
print(type(d))
print(type(e))
print(a)
print(b)
print(c)
print(d)
print(e)
# 关闭连接
client_socket.close()