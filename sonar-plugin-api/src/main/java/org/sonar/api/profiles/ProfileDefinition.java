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

import org.sonar.api.ServerExtension;

/**
 * @since 2.3
 */
public abstract class ProfileDefinition implements ServerExtension {

  private String name;
  private String language;

  protected ProfileDefinition() {
  }

  protected ProfileDefinition(String name, String language) {
    this.name = name;
    this.language = language;
  }

  public final String getName() {
    return name;
  }

  public final ProfileDefinition setName(String s) {
    this.name = s;
    return this;
  }

  public final String getLanguage() {
    return language;
  }

  public final ProfileDefinition setLanguage(String s) {
    this.language = s;
    return this;
  }

  public abstract ProfilePrototype createPrototype();


  @Override
  public final String toString() {
    return new StringBuilder().append("[name=").append(name).append(",language=").append(language).append("]").toString();
  }
}