package io.kubesphere.jenkins.kubespheresync;

import io.kubernetes.client.ApiClient;
import io.kubernetes.client.Configuration;
import io.kubernetes.client.util.Config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class KubeSphereUtils {
  private final static Logger logger = Logger.getLogger(KubeSphereUtils.class.getName());

  private static ApiClient kubesphereClient;

  public synchronized static ApiClient getAuthenticatedKubeSphereClient() {

    String kubeconfig = CredentialsUtils.getCurrentKubeconfig();
    if (kubeconfig.length() > 0) {
      try {
        kubesphereClient = Config.fromConfig(new ByteArrayInputStream(kubeconfig.getBytes(StandardCharsets.UTF_8)));
        Configuration.setDefaultApiClient(kubesphereClient);
        kubesphereClient.getHttpClient().setConnectTimeout(30, TimeUnit.SECONDS);
        kubesphereClient.getHttpClient().setReadTimeout(5,TimeUnit.MINUTES);

      } catch (IOException e) {
        logger.warning("could not get kubeconfig");
        return null;
      }
    } else {
      logger.warning("could not get kubeconfig");
      return null;
    }

    return kubesphereClient;
  }
}
