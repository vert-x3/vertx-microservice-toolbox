package io.vertx.servicediscovery.grpc.impl;

import io.grpc.ManagedChannel;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.grpc.VertxChannelBuilder;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.grpc.GrpcService;
import io.vertx.servicediscovery.types.AbstractServiceReference;
import io.vertx.servicediscovery.utils.ClassLoaderUtils;

import java.lang.reflect.Constructor;
import java.util.Objects;

/**
 * Implementation of the GRPC service type.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class GrpcServiceImpl<T> implements GrpcService {

  @Override
  public String name() {
    return TYPE;
  }

  @Override
  public ServiceReference get(Vertx vertx, ServiceDiscovery discovery, Record record,
                              JsonObject conf) {
    Objects.requireNonNull(vertx);
    Objects.requireNonNull(record);
    Objects.requireNonNull(discovery);
    return new GrpcServiceImpl.GrpcServiceReference(vertx, discovery, record, null);
  }

  /**
   * Implementation of {@link ServiceReference} for event bus service proxies.
   */
  public class GrpcServiceReference extends AbstractServiceReference<T> {

    private final String stub;
    private final VertxChannelBuilder builder;

    /**
     * Creates a new instance of {@link GrpcServiceReference}.
     *
     * @param vertx      the vert.x instance, must not be {@code null}
     * @param discovery  the service discovery instance, must not be {@code null}
     * @param record     the service record, must not be {@code null}
     * @param customizer the method used to customize the GRPC channel
     */
    GrpcServiceReference(Vertx vertx, ServiceDiscovery discovery, Record record,
                         Handler<VertxChannelBuilder> customizer) {
      super(vertx, discovery, record);
      this.stub = record.getMetadata().getString("stub.class");
      Objects.requireNonNull(stub);

      builder = VertxChannelBuilder
        .forAddress(vertx,
          record().getLocation().getString("host"),
          record().getLocation().getInteger("port")
        );

      if (customizer != null) {
        customizer.handle(builder);
      }
    }

    /**
     * Methods used to customize the channel.
     *
     * @param handler the customizer method
     * @return the current reference
     */
    public GrpcServiceReference customizeChannel(Handler<VertxChannelBuilder> handler) {
      handler.handle(builder);
      return this;
    }

    /**
     * Build the stub object and return it. If already built, it returns the cached one.
     *
     * @return the stub instance
     */
    @Override
    public synchronized T retrieve() {
      Class<T> itf = ClassLoaderUtils.load(stub, this.getClass().getClassLoader());
      if (itf == null) {
        throw new IllegalStateException("Cannot load stub class " + stub);
      } else {
        // Create the channel
        ManagedChannel channel = builder.build();

        Class[] args = new Class[]{io.grpc.Channel.class};
        try {
          Constructor<T> constructor;
          try {
            constructor = itf.getConstructor(args);
          } catch (NoSuchMethodException ignore) {
            // Try to find the private version of the constructor.
            constructor = itf.getDeclaredConstructor(args);
            if (!constructor.isAccessible()) {
              constructor.setAccessible(true);
            }
          }
          return constructor.newInstance(channel);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }
    }
  }
}
