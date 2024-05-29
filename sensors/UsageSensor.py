from random import randrange
import time
import zmq
import logging

TOPIC = 'USAGE'

context = zmq.Context()
socket = context.socket(zmq.PUB)
socket.bind("tcp://*:5555")

while True: 
    data = randrange(100)
    socket.send_string("%s %d" % (TOPIC, data))
    logging.info("Usage sensor send %d" % data)
    time.sleep(1)