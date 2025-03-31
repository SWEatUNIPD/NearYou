package io.github.sweatunipd.service;

import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class KafkaTopicService {
    private final Admin admin;
    private static final Logger LOG = LoggerFactory.getLogger(KafkaTopicService.class);

    public KafkaTopicService(Admin admin) {
        this.admin = admin;
    }

    /**
     * Method that creates a single topic
     *
     * @param topicName         name of the topic
     * @param numPartitions     number of partitions of the topic
     * @param replicationFactor number of replication factor of the topic
     */
    public void createTopic(String topicName, int numPartitions, short replicationFactor) {
        try {
            Set<String> kafkaExistingTopics = admin.listTopics().names().get();
            if (!kafkaExistingTopics.contains(topicName)) {
                NewTopic newTopic = new NewTopic(topicName, numPartitions, replicationFactor).configs(Map.of(TopicConfig.RETENTION_MS_CONFIG, "8640000"));
                admin.createTopics(Collections.singleton(newTopic));
                LOG.info("Created topic: " + topicName);
            } else {
                LOG.info("Topic already exists: " + topicName);
            }
        } catch (ExecutionException | InterruptedException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Method that creates a list of topic names with one partition and a replication factor of 1
     *
     * @param topicNames name of the topics
     */
    public void createTopics(String... topicNames) {
        for (String topicName : topicNames) {
            createTopic(topicName, 1, (short) 1);
        }
    }
}
