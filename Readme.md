docker run -d --name broker -p 9090:9090 apache/kafka:latest
docker exec --workdir /opt/kafka/bin/ -it broker sh
./kafka-topics.sh --bootstrap-server localhost:9092 --create --topic notificationsystem