from random import randrange
import time
import zmq
import logging

TOPIC = 'TEMPERATURE'

logging.basicConfig(level = logging.INFO)

context = zmq.Context()
socket = context.socket(zmq.PUB)
socket.bind("tcp://*:5556")

while True: 
    data = randrange(80)
    socket.send_string("%s %d" % (TOPIC, data))
    logging.info("Tempature sensor send %d" % data)
    time.sleep(1)
