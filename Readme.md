/opt/homebrew/bin/zookeeper-server-start /opt/homebrew/etc/zookeeper/zoo.cfg
/opt/homebrew/bin/kafka-server-stop
/opt/homebrew/bin/kafka-server-start /opt/homebrew/etc/kafka/server.properties

// Delete kafka-logs if needed

docker run -d --name elastic-test -p 9200:9200 -e "discovery.type=single-node" -e "xpack.security.enabled=false" docker.elastic.co/elasticsearch/elasticsearch:8.8.2

curl -XDELETE 'localhost:9200/sms_request'