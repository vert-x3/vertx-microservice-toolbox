package io.vertx.discovery.test.grpc.producer;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.discovery.test.grpc.Messages;
import io.vertx.discovery.test.grpc.ProducerServiceGrpc;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.servicediscovery.Record;
import io.vertx.servicediscovery.ServiceDiscovery;
import io.vertx.servicediscovery.ServiceReference;
import io.vertx.servicediscovery.grpc.GrpcService;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class HelloTest {

  private Vertx vertx;
  private ServiceDiscovery discovery;

  @Before
  public void setup(TestContext tc) {
    vertx = Vertx.vertx();
    discovery = ServiceDiscovery.create(vertx);

    vertx.exceptionHandler(tc.exceptionHandler());
    vertx.deployVerticle(Server.class.getName(), tc.asyncAssertSuccess());
  }

  @After
  public void tearDown() {
    discovery.close();
    vertx.close();
  }

  @Test
  public void testPublicationAndConsumption(TestContext tc) {
    Record record = GrpcService.createRecord("hello", "localhost", 8080,
      ProducerServiceGrpc.ProducerServiceVertxStub.class, new JsonObject().put("foo", "bar"));
    Async async = tc.async();

    discovery.publish(record, ar -> {
      assertThat(ar.succeeded()).isTrue();
      GrpcService.getServiceStub(discovery,
        rec -> rec.getName().equalsIgnoreCase("hello"),
        ProducerServiceGrpc.ProducerServiceVertxStub.class,
        channel -> channel.usePlaintext(true),
        x -> {
          assertThat(x.succeeded()).isTrue();
          ProducerServiceGrpc.ProducerServiceVertxStub stub = x.result();
          assertService(async, stub);

        });
    });
  }

  private void assertService(Async async,
                             ProducerServiceGrpc.ProducerServiceVertxStub stub) {
    stub.streamingInputCall(exchange -> {
      exchange
        .handler(ar -> {
          if (ar.succeeded()) {
            async.complete();
          } else {
            throw new RuntimeException(ar.cause());
          }
        });

      for (int i = 0; i < 10; i++) {
        exchange.write(Messages.StreamingInputCallRequest.newBuilder().build());
      }

      exchange.end();
    });
  }

  @Test
  public void testPublicationAndConsumptionUsingLowLevelAPI(TestContext tc) {
    Record record = GrpcService.createRecord("hello", "localhost", 8080,
      ProducerServiceGrpc.ProducerServiceVertxStub.class, new JsonObject().put("foo", "bar"));
    Async async = tc.async();

    discovery.publish(record, ar -> {
      assertThat(ar.succeeded()).isTrue();

      discovery.getRecord(rec -> rec.getName().equalsIgnoreCase("hello"),
        rec -> {
          assertThat(rec.succeeded()).isTrue();
          ServiceReference reference = discovery.getReference(rec.result());
          ProducerServiceGrpc.ProducerServiceVertxStub stub =
            GrpcService.customize(reference, builder -> builder.usePlaintext(true)).get();
          assertService(async, stub);
        });
    });
  }

}
