package io.github.sweatunipd.config;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import io.github.sweatunipd.requests.NearestPOIRequest;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.common.KafkaFuture;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@ExtendWith(MockitoExtension.class)
public class KafkaTopicConfigTest {
    @Mock private Admin admin;
    @Mock private ListTopicsResult listTopicsResult;
    @Mock private KafkaFuture<Set<String>> names;

    private KafkaTopicConfig kafkaTopicConfig;

    @BeforeEach
    public void init() {
        kafkaTopicConfig = new KafkaTopicConfig(admin);
    }

    @Test
    @DisplayName("Test the creation of a topic")
    public void testTopicCreation() throws ExecutionException, InterruptedException {
        Mockito.when(admin.listTopics()).thenReturn(listTopicsResult);
        Mockito.when(listTopicsResult.names()).thenReturn(names);
        Mockito.when(names.get()).thenReturn(Collections.emptySet());
        Mockito.when(admin.createTopics(Mockito.any())).thenReturn(Mockito.mock(CreateTopicsResult.class));
        Logger logger = (Logger) LoggerFactory.getLogger(KafkaTopicConfig.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        kafkaTopicConfig.createTopic("test", 1, (short) 1);

        List<ILoggingEvent> logsList = listAppender.list;
        Assertions.assertEquals(Level.INFO, logsList.get(0).getLevel());
        Assertions.assertEquals("Created topic: test", logsList.get(0).getMessage());
    }

    @Test
    @DisplayName("Test the creation of multiple topics")
    public void testTopicsCreation() throws ExecutionException, InterruptedException {
        Mockito.when(admin.listTopics()).thenReturn(listTopicsResult);
        Mockito.when(listTopicsResult.names()).thenReturn(names);
        Mockito.when(names.get()).thenReturn(Collections.emptySet());
        Mockito.when(admin.createTopics(Mockito.any())).thenReturn(Mockito.mock(CreateTopicsResult.class));
        Logger logger = (Logger) LoggerFactory.getLogger(KafkaTopicConfig.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        kafkaTopicConfig.createTopics("test1", "test2");

        List<ILoggingEvent> logsList = listAppender.list;
        Assertions.assertEquals(2, logsList.size());
        Assertions.assertEquals(Level.INFO, logsList.get(0).getLevel());
        Assertions.assertEquals("Created topic: test1", logsList.get(0).getMessage());
        Assertions.assertEquals(Level.INFO, logsList.get(1).getLevel());
        Assertions.assertEquals("Created topic: test2", logsList.get(1).getMessage());
    }

    @Test
    @DisplayName("Test the creation of an already existing topic")
    public void testExistingTopic() throws ExecutionException, InterruptedException {
        Mockito.when(admin.listTopics()).thenReturn(listTopicsResult);
        Mockito.when(listTopicsResult.names()).thenReturn(names);
        Mockito.when(names.get()).thenReturn(Collections.singleton("test"));
        Logger logger = (Logger) LoggerFactory.getLogger(KafkaTopicConfig.class);
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);

        kafkaTopicConfig.createTopic("test", 1, (short) 1);

        List<ILoggingEvent> logsList = listAppender.list;
        Assertions.assertEquals(Level.INFO, logsList.get(0).getLevel());
        Assertions.assertEquals("Topic already exists: test", logsList.get(0).getMessage());
    }
}
