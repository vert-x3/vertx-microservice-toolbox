package io.vertx.discovery.test.grpc.consumption;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.discovery.test.grpc.ConsumerServiceGrpc;
import io.vertx.discovery.test.grpc.Messages;
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

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@RunWith(VertxUnitRunner.class)
public class ConsumptionTest {

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
      ConsumerServiceGrpc.ConsumerServiceVertxStub.class, new JsonObject().put("foo", "bar"));
    Async async = tc.async();
    AtomicInteger counter = new AtomicInteger();

    discovery.publish(record, ar -> {
      assertThat(ar.succeeded()).isTrue();
      GrpcService.getServiceStub(discovery,
        rec -> rec.getName().equalsIgnoreCase("hello"),
        ConsumerServiceGrpc.ConsumerServiceVertxStub.class,
        channel -> channel.usePlaintext(true),
        x -> {
          assertThat(x.succeeded()).isTrue();
          ConsumerServiceGrpc.ConsumerServiceVertxStub stub = x.result();
          assertStream(async, counter, stub);

        });
    });
  }

  private void assertStream(Async async, AtomicInteger counter,
                            ConsumerServiceGrpc.ConsumerServiceVertxStub stub) {
    // Make a request
    Messages.StreamingOutputCallRequest request = Messages
      .StreamingOutputCallRequest
      .newBuilder()
      .build();
    stub.streamingOutputCall(request, stream ->
      stream.handler(response -> {
        counter.incrementAndGet();
        if (counter.get() > 2) {
          async.complete();
        }
      }));
  }

  @Test
  public void testPublicationAndConsumptionUsingLowLevelAPI(TestContext tc) {
    Record record = GrpcService.createRecord("hello", "localhost", 8080,
      ConsumerServiceGrpc.ConsumerServiceVertxStub.class, new JsonObject().put("foo", "bar"));
    Async async = tc.async();
    AtomicInteger counter = new AtomicInteger();

    discovery.publish(record, ar -> {
      assertThat(ar.succeeded()).isTrue();

      discovery.getRecord(rec -> rec.getName().equalsIgnoreCase("hello"),
        rec -> {
          assertThat(rec.succeeded()).isTrue();
          ServiceReference reference = discovery.getReference(rec.result());
          ConsumerServiceGrpc.ConsumerServiceVertxStub stub =
            GrpcService.customize(reference, builder -> builder.usePlaintext(true)).get();
          assertStream(async, counter, stub);
        });
    });
  }

}
