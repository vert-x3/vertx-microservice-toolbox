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

/** @module vertx-service-discovery-js/discovery_bridge */
var utils = require('vertx-js/util/utils');
var ServiceDiscovery = require('vertx-service-discovery-js/service_discovery');
var Vertx = require('vertx-js/vertx');
var Future = require('vertx-js/future');

var io = Packages.io;
var JsonObject = io.vertx.core.json.JsonObject;
var JDiscoveryBridge = io.vertx.servicediscovery.spi.DiscoveryBridge;

/**
 Discovery bridge allows integrate other discovery technologies with the Vert.x discovery service. It maps entries
 @class
*/
var DiscoveryBridge = function(j_val) {

  var j_discoveryBridge = j_val;
  var that = this;

  /**
   Starts the bridge.

   @public
   @param vertx {Vertx} the vertx instance 
   @param discovery {ServiceDiscovery} the service discovery 
   @param configuration {Object} the bridge configuration if any 
   @param future {Future} a future on which the bridge must report the completion of the starting 
   */
  this.start = function(vertx, discovery, configuration, future) {
    var __args = arguments;
    if (__args.length === 4 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'object' && __args[1]._jdel && (typeof __args[2] === 'object' && __args[2] != null) && typeof __args[3] === 'object' && __args[3]._jdel) {
      j_discoveryBridge["start(io.vertx.core.Vertx,io.vertx.servicediscovery.spi.ServiceDiscovery,io.vertx.core.json.JsonObject,io.vertx.core.Future)"](vertx._jdel, discovery._jdel, utils.convParamJsonObject(configuration), future._jdel);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  /**
   Stops the bridge.

   @public
   @param vertx {Vertx} the vertx instance 
   @param discovery {ServiceDiscovery} the service discovery 
   @param future {Future} the future on which the bridge must report the completion of the stopping process 
   */
  this.stop = function(vertx, discovery, future) {
    var __args = arguments;
    if (__args.length === 3 && typeof __args[0] === 'object' && __args[0]._jdel && typeof __args[1] === 'object' && __args[1]._jdel && typeof __args[2] === 'object' && __args[2]._jdel) {
      j_discoveryBridge["stop(io.vertx.core.Vertx,io.vertx.servicediscovery.spi.ServiceDiscovery,io.vertx.core.Future)"](vertx._jdel, discovery._jdel, future._jdel);
    } else throw new TypeError('function invoked with invalid arguments');
  };

  // A reference to the underlying Java delegate
  // NOTE! This is an internal API and must not be used in user code.
  // If you rely on this property your code is likely to break if we change it / remove it without warning.
  this._jdel = j_discoveryBridge;
};

// We export the Constructor function
module.exports = DiscoveryBridge;