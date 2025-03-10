package io.github.sweatunipd;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.KafkaFuture;

import java.util.Collections;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class KafkaAdmin {
    private AdminClient adminClient;

    public KafkaAdmin() {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9094");
        adminClient= AdminClient.create(props);
    }

    public void createTopic(String topicName, int partitions) throws ExecutionException, InterruptedException {
        NewTopic topic=new NewTopic(topicName, partitions, (short)(1)); //TODO: replication factor impostato a 1
        CreateTopicsResult result = adminClient.createTopics(Collections.singleton(topic));
        KafkaFuture<Void> future = result.values().get(topicName);
        future.get();
    }
}
