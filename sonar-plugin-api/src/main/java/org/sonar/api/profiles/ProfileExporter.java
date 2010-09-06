/*
 * Sonar, open source software quality management tool.
 * Copyright (C) 2009 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * Sonar is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * Sonar is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with Sonar; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02
 */
package org.sonar.api.profiles;

import org.sonar.api.BatchExtension;
import org.sonar.api.ServerExtension;

import java.io.Writer;

/**
 * @since 2.3
 */
public abstract class ProfileExporter implements BatchExtension, ServerExtension {

  private String[] supportedRepositories = new String[0];

  public abstract void exportProfile(RulesProfile profile, Writer writer);

  protected final void setSupportedRepositories(String... repositoryKeys) {
    supportedRepositories = (repositoryKeys != null ? repositoryKeys : new String[0]);
  }

  /**
   * @return if empty, then all repositories are supported.
   */
  public final String[] getSupportedRepositories() {
    return supportedRepositories;
  }
}
