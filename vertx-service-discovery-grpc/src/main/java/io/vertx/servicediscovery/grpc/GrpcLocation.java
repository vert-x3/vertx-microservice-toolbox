/*
 * Copyright (c) 2011-2016 The original author or authors
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution.
 *
 *      The Eclipse Public License is available at
 *      http://www.eclipse.org/legal/epl-v10.html
 *
 *      The Apache License v2.0 is available at
 *      http://www.opensource.org/licenses/apache2.0.php
 *
 * You may elect to redistribute this code under either of these licenses.
 */

package io.vertx.servicediscovery.grpc;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.json.JsonObject;

/**
 * Represents the location of a GRPC endpoint. This object (its json representation) will be
 * used as "location" in a service record.
 *
 * @author <a href="http://escoffier.me">Clement Escoffier</a>
 */
@DataObject(generateConverter = true)
public class GrpcLocation {

  private String host;
  private int port;
  private String endpoint;
  private boolean ssl = false;


  /**
   * Creates a new {@link GrpcLocation} instance.
   */
  public GrpcLocation() {
    // empty constructor
  }

  /**
   * Creates a new {@link GrpcLocation} instance by copying another instance.
   *
   * @param other the instance fo copy
   */
  public GrpcLocation(GrpcLocation other) {
    this.host = other.host;
    this.port = other.port;
    this.ssl = other.ssl;
  }

  /**
   * Creates a new {@link GrpcLocation} from the given json object
   *
   * @param json the json object
   */
  public GrpcLocation(JsonObject json) {
    this();
    GrpcLocationConverter.fromJson(json, this);
  }

  /**
   * @return a json representation of the current {@link GrpcLocation}.
   */
  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    GrpcLocationConverter.toJson(this, json);
    return json;
  }

  /**
   * @return the host
   */
  public String getHost() {
    return host;
  }

  /**
   * Sets the host.
   *
   * @param host the host
   * @return the current {@link GrpcLocation}
   */
  public GrpcLocation setHost(String host) {
    this.host = host;
    updateLocation();
    return this;
  }

  /**
   * Sets the endpoint, which is the URL of the service. The endpoint is automatically
   * computed when you use the other `setX` method.
   *
   * @param endpoint the endpoint
   * @return the current {@link GrpcLocation}
   */
  public GrpcLocation setEndpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  /**
   * @return the URL of the service
   */
  public String getEndpoint() {
    return endpoint;
  }

  /**
   * @return the port.
   */
  public int getPort() {
    return port;
  }

  /**
   * Sets the port
   *
   * @param port the port
   * @return the current {@link GrpcLocation}
   */
  public GrpcLocation setPort(int port) {
    this.port = port;
    updateLocation();
    return this;
  }

  private void updateLocation() {
    setEndpoint("http" + (isSsl() ? "s" : "") + "://" + host + ":" + port);
  }

  /**
   * Sets whether or not the GRPC service is expecting a TLS connection.
   *
   * @param ssl {@code true} to denotes that the service use {@code TLS}
   * @return the current {@link GrpcLocation}
   */
  public GrpcLocation setSsl(boolean ssl) {
    this.ssl = ssl;
    updateLocation();
    return this;
  }

  /**
   * @return {@code true} if the location is using {@code https}, {@code false} otherwise.
   */
  public boolean isSsl() {
    return ssl;
  }
}
