from random import randrange
import time
import zmq
import logging
import os

TOPIC = 'USAGE'

PORT = os.getenv('PORT', '5555')

logging.basicConfig(level = logging.INFO)

context = zmq.Context()
socket = context.socket(zmq.PUB)
socket.bind("tcp://*:%s" % PORT)

while True: 
    data = randrange(100)
    socket.send_string("%s %d" % (TOPIC, data))
    logging.info("Usage sensor send %d" % data)
    time.sleep(1)
