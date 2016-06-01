/*
 * Copyright 2014 Red Hat, Inc.
 *
 * Red Hat licenses this file to you under the Apache License, version 2.0
 * (the "License"); you may not use this file except in compliance with the
 * License.  You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package io.vertx.rxjava.servicediscovery.spi;

import java.util.Map;
import rx.Observable;
import io.vertx.servicediscovery.Record;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 *
 * <p/>
 * NOTE: This class has been automatically generated from the {@link io.vertx.servicediscovery.spi.ServiceDiscovery original} non RX-ified interface using Vert.x codegen.
 */

public class ServiceDiscovery {

  final io.vertx.servicediscovery.spi.ServiceDiscovery delegate;

  public ServiceDiscovery(io.vertx.servicediscovery.spi.ServiceDiscovery delegate) {
    this.delegate = delegate;
  }

  public Object getDelegate() {
    return delegate;
  }

  /**
   * Publishes a record.
   * @param record the record
   * @param resultHandler handler called when the operation has completed (successfully or not). In case of success, the passed record has a registration id required to modify and un-register the service.
   */
  public void publish(Record record, Handler<AsyncResult<Record>> resultHandler) { 
    delegate.publish(record, resultHandler);
  }

  /**
   * Publishes a record.
   * @param record the record
   * @return 
   */
  public Observable<Record> publishObservable(Record record) { 
    io.vertx.rx.java.ObservableFuture<Record> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    publish(record, resultHandler.toHandler());
    return resultHandler;
  }

  /**
   * Un-publishes a record.
   * @param id the registration id
   * @param resultHandler handler called when the operation has completed (successfully or not).
   */
  public void unpublish(String id, Handler<AsyncResult<Void>> resultHandler) { 
    delegate.unpublish(id, new Handler<AsyncResult<java.lang.Void>>() {
      public void handle(AsyncResult<java.lang.Void> ar) {
        if (ar.succeeded()) {
          resultHandler.handle(io.vertx.core.Future.succeededFuture(ar.result()));
        } else {
          resultHandler.handle(io.vertx.core.Future.failedFuture(ar.cause()));
        }
      }
    });
  }

  /**
   * Un-publishes a record.
   * @param id the registration id
   * @return 
   */
  public Observable<Void> unpublishObservable(String id) { 
    io.vertx.rx.java.ObservableFuture<Void> resultHandler = io.vertx.rx.java.RxHelper.observableFuture();
    unpublish(id, resultHandler.toHandler());
    return resultHandler;
  }


  public static ServiceDiscovery newInstance(io.vertx.servicediscovery.spi.ServiceDiscovery arg) {
    return arg != null ? new ServiceDiscovery(arg) : null;
  }
}
