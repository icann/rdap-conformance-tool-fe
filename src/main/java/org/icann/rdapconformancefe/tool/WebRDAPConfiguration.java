package org.icann.rdapconformancefe.tool;

import java.io.File;
import java.net.URI;
import org.apache.commons.lang3.SystemUtils;
import org.icann.rdapconformance.validator.configuration.*;
import org.icann.rdapconformance.validator.workflow.rdap.RDAPQueryType;

public class WebRDAPConfiguration implements RDAPValidatorConfiguration {
  private URI uri;
  private int timeout;
  private int maxRedirects;
  private boolean useLocalDatasets;
  private boolean useRdapProfileFeb2019;
  private boolean isGtldRegistrar;
  private boolean isGtldRegistry;
  private boolean isThin;
  private boolean isVerbose;
  private RDAPQueryType queryType;
  private boolean dependantRdapProfileGtld;
  private String configurationFile;

  public void setConfigurationFile(String configurationFile) {
    this.configurationFile = configurationFile;
  }

  public void setVerbose(boolean isVerbose) {
    this.isVerbose = isVerbose;
  }

  public boolean isVerbose() {
    return this.isVerbose;
  }

  @Override
  public URI getUri() {
    return this.uri;
  }

  @Override
  public void setUri(URI uri) {
    this.uri = uri;
  }

  @Override
  public int getTimeout() {
    return this.timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  @Override
  public int getMaxRedirects() {
    return this.maxRedirects;
  }

  public void setMaxRedirects(int maxRedirects) {
    this.maxRedirects = maxRedirects;
  }

  public boolean isUseLocalDatasets() {
    return this.useLocalDatasets;
  }

  public void setUseLocalDatasets(boolean useLocalDatasets) {
    this.useLocalDatasets = useLocalDatasets;
  }

  @Override
  public boolean useRdapProfileFeb2019() {
    return this.useRdapProfileFeb2019;
  }

  @Override
  public boolean isGtldRegistrar() {
    return this.isGtldRegistrar;
  }

  @Override
  public boolean isGtldRegistry() {
    return this.isGtldRegistry;
  }

  public void setGtldRegistrar(boolean isGtldRegistrar) {
    this.isGtldRegistrar = isGtldRegistrar;
  }

  public void setGtldRegistry(boolean isGtldRegistry) {
    this.isGtldRegistry = isGtldRegistry;
  }

  @Override
  public boolean isThin() {
    return this.isThin;
  }

  public void setThin(boolean isThin) {
    this.isThin = isThin;
  }

  @Override
  public RDAPQueryType getQueryType() {
    return this.queryType;
  }

  public void setQueryType(RDAPQueryType queryType) {
    this.queryType = queryType;
  }

  @Override
  public URI getConfigurationFile() {
    try {
      return URI.create(this.configurationFile);
    } catch (IllegalArgumentException ex) {
      // handle Windows uri without compromising remote file:
      if (SystemUtils.IS_OS_WINDOWS) {
        return new File(this.configurationFile).toURI();
      }

      throw ex;
    }
  }

  @Override
  public boolean useLocalDatasets() {
    return this.useLocalDatasets;
  }
}
