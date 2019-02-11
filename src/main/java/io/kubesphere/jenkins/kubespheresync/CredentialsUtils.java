package io.kubesphere.jenkins.kubespheresync;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import com.cloudbees.plugins.credentials.domains.DomainRequirement;
import com.microsoft.jenkins.kubernetes.credentials.KubeconfigCredentials;
import hudson.security.ACL;
import jenkins.model.Jenkins;

import java.util.Collections;

public class CredentialsUtils {

  public static String getCurrentKubeconfig() {
    String credentialsId = GlobalPluginConfiguration.get().getCredentialsId();
    if (credentialsId.equals("")) {
      return "";
    }

    KubeconfigCredentials kubeconfigCredentials = CredentialsMatchers
      .firstOrNull(
        CredentialsProvider.lookupCredentials(KubeconfigCredentials.class, Jenkins.getActiveInstance(),
          ACL.SYSTEM, Collections.<DomainRequirement>emptyList()),
        CredentialsMatchers.withId(credentialsId));

    if (kubeconfigCredentials != null) {
      return kubeconfigCredentials.getContent();
    }

    return "";
  }
}
