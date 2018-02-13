package io.vertx.servicediscovery.grpc;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.grpc.VertxChannelBuilder;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.grpc.impl.GrpcServiceImpl;
import io.vertx.servicediscovery.spi.ServiceType;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a GRPC service.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public interface GrpcService extends ServiceType {

  /**
   * Name of the type.
   */
  String TYPE = "grpc-service";
  String STUB_CLASS_PROP = "stub.class";

  /**
   * Sugar method to creates a record for this type.
   * <p>
   * The java interface is added to the metadata in the `stub.class` key. SSL is marked as
   * {@code false}.
   *
   * @param name      the name of the service.
   * @param host      the service host
   * @param port      the service port
   * @param stubClass the Java stub class
   * @param metadata  the metadata
   * @return the created record
   */
  @GenIgnore
  static Record createRecord(String name, String host, int port, Class stubClass,
                             JsonObject metadata) {
    return createRecord(name, host, port, false, stubClass.getName(), metadata);
  }

  /**
   * Sugar method to creates a record for this type.
   * <p>
   * The java interface is added to the metadata in the `stub.class` key.
   *
   * @param name      the name of the service.
   * @param host      the service host
   * @param port      the service port
   * @param ssl       whether or not the service must be accessed using a TLS connection
   * @param stubClass the Java stub class
   * @param metadata  the metadata
   * @return the created record
   */
  @GenIgnore
  static Record createRecord(String name, String host, int port, boolean ssl, Class stubClass,
                             JsonObject metadata) {
    return createRecord(name, host, port, ssl, stubClass.getName(), metadata);
  }

  /**
   * Sugar method to creates a record for this type.
   * <p>
   * The java interface is added to the metadata in the `stub.class` key.
   *
   * @param name      the name of the service.
   * @param host      the service host
   * @param port      the service port
   * @param stubClass the Java stub class
   * @param metadata  the metadata
   * @return the created record
   */
  static Record createRecord(String name, String host, int port, boolean ssl,
                             String stubClass, JsonObject metadata) {
    Objects.requireNonNull(name);
    Objects.requireNonNull(stubClass);
    Objects.requireNonNull(host);

    JsonObject meta;
    if (metadata == null) {
      meta = new JsonObject();
    } else {
      meta = metadata.copy();
    }

    meta.put(STUB_CLASS_PROP, stubClass);

    Record record = new Record().setName(name)
      .setType(TYPE)
      .setLocation(new GrpcLocation()
        .setSsl(ssl).setHost(host).setPort(port).toJson());

    if (metadata != null) {
      record.setMetadata(meta);
    }

    return record;
  }

  /**
   * Lookup for a service record and if found, retrieve it and return the service stub
   * (used to consume the service).
   * This is a convenient method to avoid explicit lookup and then retrieval of the service.
   * A filter based on the request interface is used.
   *
   * @param discovery     the service discovery instance
   * @param stubClass     the service stub class
   * @param resultHandler the result handler
   * @param <T>           the service interface
   * @return {@code null}
   */
  @GenIgnore // Java only
  static <T> T getStub(ServiceDiscovery discovery, Class<T> stubClass,
                       Handler<VertxChannelBuilder> customizer,
                       Handler<AsyncResult<T>> resultHandler) {
    JsonObject filter = new JsonObject().put("stub.class", stubClass.getName());
    discovery.getRecord(filter, ar -> {
      if (ar.failed()) {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      } else {
        if (ar.result() == null) {
          resultHandler.handle(Future.failedFuture("Cannot find service matching with " + filter));
        } else {
          GrpcServiceImpl.GrpcServiceReference service =
            (GrpcServiceImpl.GrpcServiceReference) discovery.getReference(ar.result());
          if (customizer != null) {
            service.customizeChannel(customizer);
          }
          Future<T> tFuture = Future.succeededFuture((T) service.get());
          resultHandler.handle(tFuture);
        }
      }
    });
    return null;
  }

  /**
   * Lookup for a service record and if found, retrieve it and return the stub object
   * (used to consume the service). This is a convenient method to avoid explicit lookup and
   * then retrieval of the service. This method requires to have the {@code stubClass} set with
   * the expected set of client. This is important for usages not using Java so you can pass the
   * expected type.
   *
   * @param discovery     the service discovery
   * @param filter        the filter
   * @param stubClass     the stub class
   * @param resultHandler the result handler
   * @param <T>           the type of the client class
   */
  static <T> void getServiceStub(ServiceDiscovery discovery,
                                 Function<Record, Boolean> filter,
                                 Class<T> stubClass,
                                 Handler<VertxChannelBuilder> customizer,
                                 Handler<AsyncResult<T>> resultHandler) {
    discovery.getRecord(filter, ar -> {
      if (ar.failed()) {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      } else {
        if (ar.result() == null) {
          resultHandler.handle(Future.failedFuture("Cannot find service matching with " + filter));
        } else {
          GrpcServiceImpl.GrpcServiceReference service =
            (GrpcServiceImpl.GrpcServiceReference) discovery.getReference(ar.result());
          if (customizer != null) {
            service.customizeChannel(customizer);
          }
          Future<T> tFuture = Future.succeededFuture((T) service.getAs(stubClass));
          resultHandler.handle(tFuture);
        }
      }
    });
  }

  /**
   * Lookup for a service record and if found, retrieve it and return the stub object
   * (used to consume the service). This is a convenient method to avoid explicit lookup
   * and then retrieval of the service. This method requires to have the {@code stubClass}
   * set with the expected set of client. This is important for usages not using Java so
   * you can pass the expected type.
   *
   * @param discovery     the service discovery
   * @param filter        the filter as json object
   * @param stubClass     the client class
   * @param resultHandler the result handler
   * @param <T>           the type of the client class
   */
  static <T> void getServiceStubWithJsonFilter(ServiceDiscovery discovery,
                                               JsonObject filter,
                                               Class<T> stubClass,
                                               Handler<VertxChannelBuilder> customizer,
                                               Handler<AsyncResult<T>> resultHandler) {
    discovery.getRecord(filter, ar -> {
      if (ar.failed()) {
        resultHandler.handle(Future.failedFuture(ar.cause()));
      } else {
        if (ar.result() == null) {
          resultHandler.handle(Future.failedFuture("Cannot find service matching with " + filter));
        } else {

          GrpcServiceImpl.GrpcServiceReference service =
            (GrpcServiceImpl.GrpcServiceReference) discovery.getReference(ar.result());
          if (customizer != null) {
            service.customizeChannel(customizer);
          }
          Future<T> tFuture = Future.succeededFuture((T) service.getAs(stubClass));
          resultHandler.handle(tFuture);
        }
      }
    });
  }

  /**
   * Allows customizing the GRPC channel before retrieving the stub.
   *
   * @param reference  the service reference, must not be {@code null}
   * @param customizer a method manipulating the channel builder
   * @return the passed service reference
   */
  static ServiceReference customize(ServiceReference reference,
                                    Handler<VertxChannelBuilder> customizer) {
    Objects.requireNonNull(reference);
    if (!(reference instanceof GrpcServiceImpl.GrpcServiceReference)) {
      throw new IllegalArgumentException("The reference is not a GRPC service reference");
    }
    if (customizer != null) {
      ((GrpcServiceImpl.GrpcServiceReference) reference).customizeChannel(customizer);
    }

    return reference;
  }
}
