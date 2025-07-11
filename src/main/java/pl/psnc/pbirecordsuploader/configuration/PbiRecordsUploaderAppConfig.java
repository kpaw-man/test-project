package pl.psnc.pbirecordsuploader.configuration;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;
import pl.psnc.pbirecordsuploader.domain.UploadRequest;
import pl.psnc.pbirecordsuploader.kafka.UploadRequestDeserializer;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@Slf4j
@EnableKafka
public class PbiRecordsUploaderAppConfig {

    public static final String KAFKA_LISTENER_CONTAINER_ID = "inputTopic_container";
    private final String bootstrapServers;
    private final boolean tlsEnabled;
    private final String serviceName;
    private final String truststoreLocation;
    private final String truststorePassword;
    private final String keystoreLocation;
    private final String keystorePassword;
    private final String inputTopic;
    private final String dltTopicSuffix;
    private final long dltBackoffInterval;
    private final long dltBackoffMaxAttempts;
    private final boolean dltEnabled;

    public PbiRecordsUploaderAppConfig(@Value("${kafka.bootstrap-servers}") String bootstrapServers,
            @Value("${spring.application.name}") String serviceName,
            @Value("${spring.kafka.ssl.trust-store-location}") String truststoreLocation,
            @Value("${spring.kafka.ssl.trust-store-password}") String truststorePassword,
            @Value("${spring.kafka.ssl.key-store-location}") String keystoreLocation,
            @Value("${spring.kafka.ssl.key-store-password}") String keystorePassword,
            @Value("${kafka.tls.enabled}") boolean tlsEnabled,
            @Value("${pbi.kafka.input-topic}") String inputTopic,
            @Value("${pbi.kafka.dlt.topic-suffix}") String dltTopicSuffix,
            @Value("${pbi.kafka.dlt.backoff-interval-ms}") long dltBackoffInterval,
            @Value("${pbi.kafka.dlt.backoff-max-attempts}") long dltBackoffMaxAttempts,
            @Value("${pbi.kafka.dlt.enabled}")boolean dltEnabled) {
        this.bootstrapServers = bootstrapServers;
        this.serviceName = serviceName;
        this.truststoreLocation = truststoreLocation;
        this.truststorePassword = truststorePassword;
        this.keystoreLocation = keystoreLocation;
        this.keystorePassword = keystorePassword;
        this.tlsEnabled = tlsEnabled;
        this.inputTopic = inputTopic;
        this.dltTopicSuffix = dltTopicSuffix;
        this.dltBackoffInterval = dltBackoffInterval;
        this.dltBackoffMaxAttempts = dltBackoffMaxAttempts;
        this.dltEnabled = dltEnabled;
    }

    @Bean
    public String dltTopic() {
        return inputTopic + dltTopicSuffix;
    }

    @Bean
    public String listeningTopic() {
        return inputTopic;
    }

    @Bean
    public String groupId() {
        return serviceName;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, UploadRequest> kafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, UploadRequest> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(consumerProperties()));
        factory.setConcurrency(1);


        if(dltEnabled) {
            DeadLetterPublishingRecoverer deadLetterPublishingRecoverer = new DeadLetterPublishingRecoverer(
                    kafkaTemplate(), (event, ex) -> new TopicPartition(dltTopic(), event.partition()));

            DefaultErrorHandler errorHandler = getDefaultErrorHandler(deadLetterPublishingRecoverer);
            factory.setCommonErrorHandler(errorHandler);
        }
        else {
            DefaultErrorHandler errorHandler = new DefaultErrorHandler(new FixedBackOff(0L, 0));
            factory.setCommonErrorHandler(errorHandler);
        }
        return factory;
    }

    private DefaultErrorHandler getDefaultErrorHandler(DeadLetterPublishingRecoverer deadLetterPublishingRecoverer) {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(deadLetterPublishingRecoverer,
                new FixedBackOff(dltBackoffInterval, dltBackoffMaxAttempts));

        errorHandler.setRetryListeners((event, exception, deliveryAttempt) -> {
            if (deliveryAttempt == dltBackoffMaxAttempts + 1) {
                log.debug("Sending message to DLT: topic={}, key={}, value={}}", event.topic(), event.key(),
                        event.value(), exception);
            }
        });
        return errorHandler;
    }

    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate() {
        return new KafkaTemplate<>(new DefaultKafkaProducerFactory<>(producerProperties()));
    }

    private Map<String, Object> consumerProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, serviceName);
        props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1);
        props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, UploadRequestDeserializer.class);
        props.putAll(getSslProperties());
        return props;
    }

    private Map<String, Object> producerProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        props.putAll(getSslProperties());
        return props;
    }

    private Map<String, Object> getSslProperties() {
        Map<String, Object> sslProps = new HashMap<>();
        if (tlsEnabled) {
            sslProps.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name);
            sslProps.put(SslConfigs.SSL_ENDPOINT_IDENTIFICATION_ALGORITHM_CONFIG, "https");
            sslProps.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG, removeUrlResourcePrefix(truststoreLocation));
            sslProps.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, truststorePassword);
            sslProps.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG, removeUrlResourcePrefix(keystoreLocation));
            sslProps.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, keystorePassword);
        }
        return sslProps;
    }

    private String removeUrlResourcePrefix(String location) {
        return location.startsWith("file:") ? location.replace("file:", "") : location;
    }

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(20))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
    }
}
