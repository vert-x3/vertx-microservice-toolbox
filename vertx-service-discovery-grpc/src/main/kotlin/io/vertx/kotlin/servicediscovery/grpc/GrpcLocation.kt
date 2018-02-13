package io.vertx.kotlin.servicediscovery.grpc

import io.vertx.servicediscovery.grpc.GrpcLocation

/**
 * A function providing a DSL for building [io.vertx.servicediscovery.grpc.GrpcLocation] objects.
 *
 * Represents the location of a GRPC endpoint. This object (its json representation) will be
 * used as "location" in a service record.
 *
 * @param endpoint  Sets the endpoint, which is the URL of the service. The endpoint is automatically computed when you use the other `setX` method.
 * @param host  Sets the host.
 * @param port  Sets the port
 * @param ssl  Sets whether or not the GRPC service is expecting a TLS connection.
 *
 * <p/>
 * NOTE: This function has been automatically generated from the [io.vertx.servicediscovery.grpc.GrpcLocation original] using Vert.x codegen.
 */
fun GrpcLocation(
  endpoint: String? = null,
  host: String? = null,
  port: Int? = null,
  ssl: Boolean? = null): GrpcLocation = io.vertx.servicediscovery.grpc.GrpcLocation().apply {

  if (endpoint != null) {
    this.setEndpoint(endpoint)
  }
  if (host != null) {
    this.setHost(host)
  }
  if (port != null) {
    this.setPort(port)
  }
  if (ssl != null) {
    this.setSsl(ssl)
  }
}

