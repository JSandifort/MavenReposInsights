package io.sandifort.kafkadownloader.configuration;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.multibindings.ProvidesIntoSet;
import dev.c0ps.diapper.IInjectorConfig;
import dev.c0ps.diapper.InjectorConfig;
import dev.c0ps.franz.Kafka;
import dev.c0ps.franz.KafkaErrors;
import dev.c0ps.franz.KafkaImpl;
import dev.c0ps.io.JsonUtils;
import dev.c0ps.io.JsonUtilsImpl;
import dev.c0ps.io.ObjectMapperBuilder;
import dev.c0ps.maveneasyindex.ArtifactModule;
import io.sandifort.kafkadownloader.kafka.utils.KafkaUtils;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.ext.ContextResolver;
import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

@InjectorConfig
public class InsightsConfig implements IInjectorConfig {

    private static final Logger LOG = LoggerFactory.getLogger(InsightsConfig.class);

    private final InsightsArgs args;

    public InsightsConfig(InsightsArgs args) {
        this.args = args;
    }

    public Client client;
    @Override
    public void configure(Binder binder) {

        binder.bind(InsightsArgs.class).toInstance(args);
        client = setupClient();
    }

    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();
    private static final AtomicBoolean IS_RUNNING = new AtomicBoolean(true);
    private static final KafkaImpl KAFKA = KafkaUtils.getKafkaInstance();

    private Client setupClient() {
        var list = new HashSet<SimpleModule>();
        list.add(new ArtifactModule());
        list.add(new SimpleErrorModule());

        var om = new ObjectMapper().registerModules(list);
        var config = new ClientConfig().register(new ContextResolver<ObjectMapper>() {
            @Override
            public ObjectMapper getContext(Class<?> type) {
                return om;
            }
        });
        return ClientBuilder.newClient(config);
    }

    @Provides
    @Singleton
    public JsonUtils bindJsonUtils(ObjectMapper om) {
        return new JsonUtilsImpl(om);
    }

    @ProvidesIntoSet
    public Module provideArtifactModule() {
        return new ArtifactModule();
    }

    @ProvidesIntoSet
    public Module provideSimpleErrorModule() {
        return new SimpleErrorModule();
    }

    @Provides
    @Singleton
    public ObjectMapper bindObjectMapper(Set<Module> modules) {
        LOG.info("Instantiating ObjectMapper from {} modules: {}", modules.size(), modules);

        return new ObjectMapperBuilder().build() //
                .registerModules(modules);
    }

    @Singleton
    @Provides
    public ExecutorService bindExecutorService() {
        return EXEC;
    }

    @Singleton
    @Provides
    public AtomicBoolean bindIsRunning() {
        return IS_RUNNING;
    }

    @Singleton
    @Provides
    public Kafka bindKafka() {
        return KAFKA;
    }

    @Singleton
    @Provides
    public KafkaErrors bindKafkaErrors() {
        return KAFKA;
    }

    @Provides
    @Named("MavenRepoDirectory")
    public String bindMavenRepoDirectory() {
        return args.mavenRepoDirectory;
    }

    @Provides
    @Named("mavenCmdPath")
    public String bindMavenCmdPath() {
        return args.mavenCmdPath;
    }

    @Provides
    @Named("mavenHomePath")
    public String bindMavenHomePath() {
        return args.mavenHomePath;
    }

    @Provides
    @Named("outputDirectory")
    public String bindOutputDirectory() {
        return args.outputDirectory;
    }

    @Provides
    @Named("shouldSubscribeErrors")
    public boolean shouldSubscribeErrors() {
        return args.shouldSubscribeErrors;
    }

    @Provides
    @Named("KafkaConnector.serverUrl")
    public String bindKafkaConnectorServerUrl() {
        return "http://api.sandifort.io:19092";
    }
}
