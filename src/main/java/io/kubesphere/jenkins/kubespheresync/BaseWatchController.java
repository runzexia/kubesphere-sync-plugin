/**
 * Copyright (C) 2019 The KubeSphere Authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.kubesphere.jenkins.kubespheresync;


import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import io.kubernetes.client.models.V1ObjectMeta;
import io.kubernetes.client.util.Watch;
import io.kubesphere.jenkins.kubespheresync.watcher.WatchListener;
import jenkins.util.Timer;


public abstract class BaseWatchController<T> implements WatchListener<T> {
  private final Logger LOGGER = Logger.getLogger(BaseWatchController.class.getName());

  private static final long IGNORED_RESOURCE_VERSION = 0;

  protected ScheduledFuture relister;

  protected Watch watch;

  private Thread thread = null;

  private Long resourceVersion = 0L;

  public BaseWatchController() {
    watch = null;
  }


  public void onClose(Throwable e) {
    LOGGER.info("Watch for type " + this.getClass().getName() + " closed for one of the following namespaces: " + e.toString());
    if (e instanceof RuntimeException) {
      LOGGER.info("reconnection watch");
      stop();
      start();
      return;
    }
    stop();
  }

  public abstract Runnable getStartTimerTask();

  public abstract int getListIntervalInSeconds();

  public synchronized void start() {
    // lets do this in a background thread to avoid errors like:
    // Tried proxying
    // GlobalPluginConfiguration to support
    // a circular dependency, but it is not an interface.
    Runnable task = getStartTimerTask();
    relister = Timer.get().scheduleAtFixedRate(task, 100, // still do the
      // first run 100
      // milliseconds in
      getListIntervalInSeconds() * 1000,
      TimeUnit.MILLISECONDS);
  }

  public void stop() {
    if (relister != null && !relister.isDone()) {
      relister.cancel(true);
      relister = null;
    }
    try {
      if (watch != null) {
        watch.close();
      }
    } catch (IOException e) {
      LOGGER.warning(e.getMessage());
    } finally {
      watch = null;
    }
  }

  protected abstract void doWatch();

  protected void startWatch() {
    thread = new Thread(this::doWatch);
    thread.start();
  }

  protected boolean hasSlaveLabelOrAnnotation(Map<String, String> map) {
    if (map != null)
      return map.containsKey("role")
        && map.get("role").equals("jenkins-slave");
    return false;
  }

  protected void trackResourceVersion(String type, Object object) {
    updateResourceVersion(getNewResourceVersion(type, object));
  }

  private long getNewResourceVersion(String type, Object object) {
    long newResourceVersion = getResourceVersionFromMetadata(object);
    if (type.equalsIgnoreCase("DELETED")) return 1 + newResourceVersion;
    else return newResourceVersion;
  }

  private long getResourceVersionFromMetadata(Object object) {
    try {
      Method getMetadata = object.getClass().getDeclaredMethod("getMetadata");
      V1ObjectMeta metadata = (V1ObjectMeta) getMetadata.invoke(object);
      String val = metadata.getResourceVersion();
      return !isNullOrEmptyString(val) ? Long.parseLong(val) : 0;
    } catch (Exception e) {
      LOGGER.warning(e.getMessage());
      return IGNORED_RESOURCE_VERSION;
    }
  }

  private void updateResourceVersion(long newResourceVersion) {
    if (resourceVersion == 0) resourceVersion = newResourceVersion;
    else if (newResourceVersion > resourceVersion) resourceVersion = newResourceVersion;
  }

  protected Long getResourceVersion() {
    if (resourceVersion != null)
      return resourceVersion;
    return 0L;
  }

  private static boolean isNullOrEmptyString(String s) {
    return s == null || s.equals("");
  }

}
