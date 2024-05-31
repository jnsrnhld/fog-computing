FROM python:3.9.19-alpine3.20
COPY . /app
WORKDIR /app
RUN pip install -r requirements.txt
EXPOSE 5556/tcp
CMD ["python", "TemperatureSensor.py"]
