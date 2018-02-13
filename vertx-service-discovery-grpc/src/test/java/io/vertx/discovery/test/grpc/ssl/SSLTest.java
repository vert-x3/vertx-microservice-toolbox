package io.vertx.discovery.test.grpc.ssl;

import io.grpc.examples.helloworld.GreeterGrpc;
import io.grpc.examples.helloworld.HelloRequest;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.net.JksOptions;
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
public class SSLTest {

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
      GreeterGrpc.GreeterVertxStub.class, new JsonObject().put("foo", "bar"));
    Async async = tc.async();

    discovery.publish(record, ar -> {
      assertThat(ar.succeeded()).isTrue();
      GrpcService.getServiceStub(discovery,
        rec -> rec.getName().equalsIgnoreCase("hello"),
        GreeterGrpc.GreeterVertxStub.class,
        channel -> {
          channel.useSsl(options -> options.setSsl(true)
            .setUseAlpn(true)
            .setTrustStoreOptions(new JksOptions()
              .setPath("client-truststore.jks")
              .setPassword("wibble")));
        },
        x -> {
          assertThat(x.succeeded()).isTrue();
          GreeterGrpc.GreeterVertxStub stub = x.result();
          assertService(tc, async, stub);
        });
    });
  }

  private void assertService(TestContext tc, Async async,
                             GreeterGrpc.GreeterVertxStub stub) {
    HelloRequest request = HelloRequest.newBuilder().setName("Vert.x").build();
    stub.sayHello(request, asyncResponse -> {
      if (asyncResponse.succeeded()) {
        System.out.println("Succeeded " + asyncResponse.result().getMessage());
        async.complete();
      } else {
        tc.fail(asyncResponse.cause());
      }
    });
  }

  @Test
  public void testPublicationAndConsumptionUsingLowLevelAPI(TestContext tc) {
    Record record = GrpcService.createRecord("hello", "localhost", 8080,
      GreeterGrpc.GreeterVertxStub.class, new JsonObject().put("foo", "bar"));
    Async async = tc.async();

    discovery.publish(record, ar -> {
      assertThat(ar.succeeded()).isTrue();

      discovery.getRecord(rec -> rec.getName().equalsIgnoreCase("hello"),
        rec -> {
          assertThat(rec.succeeded()).isTrue();
          ServiceReference reference = discovery.getReference(rec.result());
          GreeterGrpc.GreeterVertxStub stub =
            GrpcService.customize(reference, builder ->
              builder.useSsl(options -> options.setSsl(true)
                .setUseAlpn(true)
                .setTrustStoreOptions(new JksOptions()
                  .setPath("client-truststore.jks")
                  .setPassword("wibble"))))
              .get();
          assertService(tc, async, stub);
        });
    });
  }

}
