# Kafka Links

### Schema Registry
http://192.168.1.151:8081/subjects/users-value/versions/1
http://192.168.1.151:8081/subjects/users-value/versions/latest

#### Uploading schema
curl -X POST -H "Content-Type: application/vnd.schemaregistry.v1+json" --data '{"schema": "{\"type\":\"record\",\"name\":\"Payment\",\"namespace\":\"io.confluent.examples.clients.basicavro\",\"fields\":[{\"name\":\"id\",\"type\":\"string\"},{\"name\":\"amount\",\"type\":\"double\"}]}"}' http://localhost:8081/subjects/test-value/versions

# Starting Kafka

## session 1
confluent local start
confluent local stop




### examples
kafka-topics --zookeeper 192.168.1.151:2181 --list
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic users
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic pageviews
kafka-topics --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic hello_world

kafka-console-consumer --bootstrap-server 192.168.1.151:9092 --topic hello_world --from-beginning


#### Dummy Data
wget https://github.com/confluentinc/kafka-connect-datagen/raw/master/config/connector_pageviews_cos.config
curl -X POST -H "Content-Type: application/json" --data @connector_pageviews_cos.config http://localhost:8083/connectors

wget https://github.com/confluentinc/kafka-connect-datagen/raw/master/config/connector_users_cos.config
curl -X POST -H "Content-Type: application/json" --data @connector_users_cos.config http://localhost:8083/connectors

#### KSQL
LOG_DIR=./ksql_logs ksql
CREATE STREAM pageviews (viewtime BIGINT, userid VARCHAR, pageid VARCHAR) WITH (KAFKA_TOPIC='pageviews', VALUE_FORMAT='AVRO');

CREATE TABLE users (registertime BIGINT, gender VARCHAR, regionid VARCHAR,  \
userid VARCHAR) \
WITH (KAFKA_TOPIC='users', VALUE_FORMAT='AVRO', KEY = 'userid');

SHOW STREAMS;
SHOW TABLES;

SET 'auto.offset.reset'='earliest';

SELECT pageid FROM pageviews EMIT LIMIT 3;
CREATE STREAM pageviews_female AS SELECT users.userid AS userid, pageid, regionid, gender FROM pageviews LEFT JOIN users ON pageviews.userid = users.userid WHERE gender = 'FEMALE';
CREATE STREAM pageviews_female_like_89 WITH (kafka_topic='pageviews_enriched_r8_r9', value_format='AVRO') AS SELECT * FROM pageviews_female WHERE regionid LIKE '%_8' OR regionid LIKE '%_9';
CREATE TABLE pageviews_regions AS SELECT gender, regionid , COUNT(*) AS numusers FROM pageviews_female WINDOW TUMBLING (size 30 second) GROUP BY gender, regionid HAVING COUNT(*) > 1;

DESCRIBE EXTENDED pageviews_female_like_89;

#### Topic related

kafka-topics.sh --zookeeper 192.168.1.151:2181 --topic <topic_name> --create --partitions <partitions> --replication-factor <replication-factor>

kafka-topics.sh --zookeeper 192.168.1.151:2181 --topic <topic_name> --delete

kafka-topics.sh --zookeeper 192.168.1.151:2181 --list

kafka-topics.sh --zookeeper 192.168.1.151:2181 --topic <topic_name> --describe

#### Producer related

kafka-console-producer.sh --broker-list 192.168.1.151:9092 --topic <topic_name> --producer-property acks=all

#### Consumer related

kafka-console-consumer.sh --bootstrap-server 192.168.1.151:9092 --topic <topic_name> --from-beginning

kafka-console-consumer.sh --bootstrap-server 192.168.1.151:9092 --topic <topic_name> --group <consumer_group_name>

kafka-console-consumer.sh --bootstrap-server 192.168.1.151:9092 --describe --topic <topic_name> --group <consumer_group_name>

#### ConsumerGroups

kafka-consumer-groups.sh --bootstrap-server 192.168.1.151:9092 --list

kafka-consumer-groups.sh --bootstrap-server 192.168.1.151:9092 --describe --group <consumer_group_name>

kafka-consumer-groups.sh --bootstrap-server 192.168.1.151:9092 --describe --group <consumer_group_name> --reset-offsets --to-earliest --execute --topic <topic_name>

kafka-consumer-groups.sh --bootstrap-server 192.168.1.151:9092 --describe --group <consumer_group_name> --reset-offsets --shift-by-2 --to-earliest --execute --topic <topic_name>


## Confluent Installation
Copy confluent platform and confluent-hub to unix box
/home/smukesh/test-env/confluent/
	confluent-x.x.x
	
curl -L https://cnfl.io/cli | sh -s -- -b ~/test-env/confluent/bin
confluent-hub install --no-prompt confluentinc/kafka-connect-datagen:latest

~/.bashrc
	export PATH=/home/msingla/test-env/confluent/confluent-5.3.2/bin:$PATH
	export CONFLUENT_HOME=/home/msingla/test-env/confluent/confluent-5.3.2


## Apache Kafka
### session 1

cd test-env/kafka_2.12-2.3.1

zookeeper-server-start.sh config/zookeeper.properties

### session 2

cd test-env/kafka_2.12-2.3.1/

kafka-server-start.sh config/server.properties

### session 3

cd test-env/kafka_2.12-2.3.1/


# Unix
sudo apt-get update
sudo apt-get upgrade
sudo apt-get dist-upgrade


