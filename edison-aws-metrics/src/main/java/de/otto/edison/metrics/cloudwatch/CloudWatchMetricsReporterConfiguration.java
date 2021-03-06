package de.otto.edison.metrics.cloudwatch;

import com.codahale.metrics.MetricRegistry;
import de.otto.edison.aws.configuration.AwsProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.core.auth.AwsCredentialsProvider;
import software.amazon.awssdk.core.regions.Region;
import software.amazon.awssdk.services.cloudwatch.CloudWatchAsyncClient;


import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties({AwsProperties.class, CloudWatchMetricsProperties.class})
@ConditionalOnProperty(name = "edison.aws.metrics.cloudWatch.enabled", havingValue = "true")
public class CloudWatchMetricsReporterConfiguration {

    private static final Logger LOG = LoggerFactory.getLogger(CloudWatchMetricsReporterConfiguration.class);

    @Bean
    public CloudWatchMetricsReporter cloudWatchReporter(final MetricRegistry metricRegistry,
                                                        final CloudWatchAsyncClient cloudWatchAsync,
                                                        final CloudWatchMetricsProperties metricsProperties) {
        final CloudWatchMetricsReporter reporter = new CloudWatchMetricsReporter(
                metricRegistry,
                metricsProperties.getAllowedMetrics(),
                metricsProperties.getNamespace(),
                cloudWatchAsync);
        reporter.start(1, TimeUnit.MINUTES);
        LOG.info("started cloudWatch metrics reporter");
        return reporter;
    }

    @Bean
    public CloudWatchAsyncClient cloudWatchAsync(final AwsCredentialsProvider awsCredentialsProvider,
                                                 final AwsProperties awsProperties) {
        return CloudWatchAsyncClient.builder()
                .region(Region.of(awsProperties.getRegion()))
                .credentialsProvider(awsCredentialsProvider)
                .build();
    }
}
