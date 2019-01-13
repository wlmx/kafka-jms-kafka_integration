# kafka-jms-kafka_integration

Пример взаимодействия на основе spring integration.
Из входящего топика(request_topic) kafka принимается сообщение, отправляется в очередь(request_queue) mq,
приходит ответ в очередь mq(response_queue) и отправляется в топик(response_topic) kafka.

см. com.example.demo.MessageProcessor

Команды для старта kafka/activeMq и отправки сообщения/просмотра топика:

activemq.bat

zookeeper-server-start.bat ../../config/zookeeper.properties
kafka-server-start.bat ../../config/server.properties

kafka-console-producer.bat --broker-list 127.0.0.1:9092 --topic request_topic
kafka-console-consumer.bat --bootstrap-server 127.0.0.1:9092 --topic response_topic --from-beginning
