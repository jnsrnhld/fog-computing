from random import randrange
import time
import zmq
import logging
import os

TOPIC = 'TEMPERATURE'

PORT = os.getenv('PORT', '5556')

logging.basicConfig(level = logging.INFO)

context = zmq.Context()
socket = context.socket(zmq.PUB)
socket.bind("tcp://*:%s" % PORT)

while True: 
    data = randrange(80)
    socket.send_string("%s %d" % (TOPIC, data))
    logging.info("Tempature sensor send %d" % data)
    time.sleep(1)
