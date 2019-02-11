package io.kubesphere.jenkins.kubespheresync.watcher;

import io.kubernetes.client.util.Watch;

/**
 * This interface is used for the final destination to deliver watch events
 *
 * @param <T> The type of the object that is being watched.
 */

public interface WatchListener<T> {
  /**
   * Call back for any watch type. This can be used instead of the specific call backs to handle any
   * type of watch response.
   *
   * @param response Watch response consisting of type and object
   */
  public void receivedResponse(Watch.Response<T> response);

  /**
   * Run when the watcher finally closes.
   *
   * @param cause What caused the watcher to be closed. Null means normal close.
   */
  void onClose(Throwable cause);
}
