package io.vertx.discovery.test.grpc.consumption;

import com.google.protobuf.ByteString;
import io.vertx.core.AbstractVerticle;
import io.vertx.discovery.test.grpc.ConsumerServiceGrpc;
import io.vertx.discovery.test.grpc.Messages;
import io.vertx.grpc.GrpcWriteStream;
import io.vertx.grpc.VertxServer;
import io.vertx.grpc.VertxServerBuilder;

import java.nio.charset.Charset;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
public class Server extends AbstractVerticle {

  @Override
  public void start() throws Exception {

    // The rcp service
    ConsumerServiceGrpc.ConsumerServiceVertxImplBase service =
      new ConsumerServiceGrpc.ConsumerServiceVertxImplBase() {
        @Override
        public void streamingOutputCall(
          Messages.StreamingOutputCallRequest request,
          GrpcWriteStream<Messages.StreamingOutputCallResponse> response
        ) {
          final AtomicInteger counter = new AtomicInteger();
          vertx.setPeriodic(1000L, t -> {
            response.write(Messages.StreamingOutputCallResponse.newBuilder().setPayload(
              Messages.Payload.newBuilder()
                .setTypeValue(Messages.PayloadType.COMPRESSABLE.getNumber())
                .setBody(ByteString.copyFrom(
                  String.valueOf(counter.incrementAndGet()), Charset.forName("UTF-8")))
            ).build());
          });
        }
      };

    // Create the server
    VertxServer rpcServer = VertxServerBuilder
      .forPort(vertx, 8080)
      .addService(service)
      .build();

    // start the server
    rpcServer.start(ar -> {
      if (ar.failed()) {
        ar.cause().printStackTrace();
      }
    });
  }
}
