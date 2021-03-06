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

/**
 */
public class Annotations {
  public static final String JENKINS_JOB_PATH = "jenkins.kubesphere.io/job-path";
  public static final String GENERATED_BY = "jenkins.kubesphere.io/generated-by";
  public static final String GENERATED_BY_JENKINS = "jenkins";
  public static final String DISABLE_SYNC_CREATE = "jenkins.kubesphere.io/disable-sync-create";
  public static final String BUILDCONFIG_NAME = "kubesphere.io/build-config.name";
}
