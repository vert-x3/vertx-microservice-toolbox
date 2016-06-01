package io.vertx.servicediscovery.spi;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.servicediscovery.Record;

/**
 * A callback interface for discovering services.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface ServiceDiscovery {

  /**
   * Publishes a record.
   *
   * @param record        the record
   * @param resultHandler handler called when the operation has completed (successfully or not). In case of success,
   *                      the passed record has a registration id required to modify and un-register the service.
   */
  void publish(Record record, Handler<AsyncResult<Record>> resultHandler);

  /**
   * Un-publishes a record.
   *
   * @param id            the registration id
   * @param resultHandler handler called when the operation has completed (successfully or not).
   */
  void unpublish(String id, Handler<AsyncResult<Void>> resultHandler);

}
