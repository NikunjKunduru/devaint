package com.nikunj.devaint.service.rabbitmq;

import com.nikunj.devaint.client.RabbitMqClient;
import com.nikunj.devaint.config.RabbitMQConfig;
import com.nikunj.devaint.model.Gpt4oQueueDTO;
import com.nikunj.devaint.util.LogConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
public class RetryService {

    @Value("${rabbitmq.retry-queues}")
    private List<String> retryQueues;

    @Value("${rabbitmq.max-retries}")
    private Integer maxRetries;

    private final RabbitMqClient rabbitMqClient;

    public RetryService(List<String> retryQueues, RabbitMqClient rabbitMqClient) {
        this.retryQueues = retryQueues;
        this.rabbitMqClient = rabbitMqClient;
    }

    public void retryMessage(Gpt4oQueueDTO queueRequest, Message message) {
        String methodName = "retryMessage()";
        log.info(LogConstants.START_METHOD, methodName);

        Integer retryCount = message.getMessageProperties().getHeader("x-retry-count");
        retryCount = (retryCount == null) ? 1 : retryCount + 1;

        log.warn(LogConstants.RETRY_ATTEMPT, retryCount, message.getMessageProperties().getCorrelationId());

        String retryQueue;
        if (retryCount > maxRetries) {
            log.error(LogConstants.MAX_RETRIES_REACHED, message.getMessageProperties().getCorrelationId());
            retryQueue = RabbitMQConfig.DLQ;
        } else {
            retryQueue = retryQueues.get(Math.min(retryCount - 1, retryQueues.size() - 1));
        }

        log.info(LogConstants.MESSAGE_SENT, retryQueue, message.getMessageProperties().getCorrelationId());

        rabbitMqClient.sendRetryMessage(retryQueue, queueRequest, retryCount);

        log.info(LogConstants.END_METHOD, methodName);
    }
}
