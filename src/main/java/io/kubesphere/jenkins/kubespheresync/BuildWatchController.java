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

import com.google.gson.reflect.TypeToken;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import hudson.triggers.SafeTimerTask;
import io.kubernetes.client.ApiException;
import io.kubernetes.client.apis.CoreV1Api;
import io.kubernetes.client.models.V1Namespace;
import io.kubernetes.client.util.Watch;
import jenkins.model.Jenkins;

import org.jenkinsci.plugins.workflow.job.WorkflowJob;


import java.util.List;
import java.util.logging.Logger;


import static java.util.logging.Level.WARNING;

public class BuildWatchController extends BaseWatchController<V1Namespace> {
  private static final Logger logger = Logger.getLogger(BuildWatchController.class
    .getName());

  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public BuildWatchController() {
    super();
  }

  @Override
  public int getListIntervalInSeconds() {
    return GlobalPluginConfiguration.get().getBuildListInterval();
  }

  @Override
  public Runnable getStartTimerTask() {
    return new SafeTimerTask() {
      @Override
      public void doRun() {
        if (watch == null) {
          startWatch();
        }
        reconcileRunsAndBuilds();
      }
    };
  }

  @Override
  public void start() {
    super.start();
  }

  @Override
  protected void doWatch() {
    CoreV1Api api = new CoreV1Api();
    try {
      watch =
        Watch.createWatch(KubeSphereUtils.getAuthenticatedKubeSphereClient(),
          api.listNamespaceCall(
            null, null, null, null, null, 5, getResourceVersion().toString(), null, Boolean.TRUE, null, null),
          new TypeToken<Watch.Response<V1Namespace>>() {
          }.getType());
    } catch (ApiException e) {
      logger.warning("could not init APICall " + e.getMessage());
      onClose(e);
    }
    Watch<V1Namespace> tempWatch = watch;
    try {
      for (Watch.Response<V1Namespace> item : tempWatch) {
        trackResourceVersion(item.type, item.object);
        receivedResponse(item);
      }
    } catch (Exception e) {
      onClose(e);
    }

  }

  @Override
  public void receivedResponse(Watch.Response<V1Namespace> response) {
    try {
      switch (response.type) {
        default:
          logger.warning("watch for build " + response.object.getMetadata().getName() + " received unknown event " + response.type);
          break;
      }
    } catch (Exception e) {
      logger.log(WARNING, "Caught: " + e, e);
    }
  }

  /**
   * Reconciles Jenkins job runs and KubeSphere builds
   *
   * Deletes all job runs that do not have an associated build in KubeSphere
   */
  private static void reconcileRunsAndBuilds() {
    logger.info("Reconciling job runs and builds");

    List<WorkflowJob> jobs = Jenkins.getActiveInstance().getAllItems(WorkflowJob.class);

//    for (WorkflowJob job : jobs) {
//      BuildConfigProjectProperty property = job.getProperty(BuildConfigProjectProperty.class);
//      if (property == null || StringUtils.isBlank(property.getNamespace()) || StringUtils.isBlank(property.getName())) {
//        continue;
//      }
//
//      logger.info("Checking job " + job.toString() + " runs for BuildConfig " + property.getNamespace() + "/" + property.getName());
//
//      BuildList buildList = getAuthenticatedKubeSphereClient().builds()
//        .inNamespace(property.getNamespace()).withLabel("buildconfig=" + property.getName()).list();
//
//      for (WorkflowRun run : job.getBuilds()) {
//        boolean found = false;
//        BuildCause cause = run.getCause(BuildCause.class);
//        for (Build build : buildList.getItems()) {
//          if (cause != null && cause.getUid().equals(build.getMetadata().getUid())) {
//            found = true;
//            break;
//          }
//        }
//        if (!found) {
//          deleteRun(run);
//        }
//      }
//    }
  }

}
