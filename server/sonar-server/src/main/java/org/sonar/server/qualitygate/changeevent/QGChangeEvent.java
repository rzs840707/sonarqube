/*
 * SonarQube
 * Copyright (C) 2009-2017 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.server.qualitygate.changeevent;

import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.sonar.api.config.Configuration;
import org.sonar.db.component.BranchDto;
import org.sonar.db.component.ComponentDto;
import org.sonar.db.component.SnapshotDto;

public class QGChangeEvent {
  private final BranchDto shortBranch;
  private final ComponentDto shortBranchComponent;
  private final SnapshotDto latestAnalysis;
  private final Configuration projectConfiguration;

  public QGChangeEvent(BranchDto shortBranch, ComponentDto shortBranchComponent, SnapshotDto latestAnalysis, Configuration projectConfiguration) {
    this.shortBranch = shortBranch;
    this.shortBranchComponent = shortBranchComponent;
    this.latestAnalysis = latestAnalysis;
    this.projectConfiguration = projectConfiguration;
  }

  public BranchDto getShortBranch() {
    return shortBranch;
  }

  public ComponentDto getShortBranchComponent() {
    return shortBranchComponent;
  }

  public SnapshotDto getLatestAnalysis() {
    return latestAnalysis;
  }

  public Configuration getProjectConfiguration() {
    return projectConfiguration;
  }

  @Override
  public String toString() {
    return "QGChangeEvent{" +
      "shortBranch=" + toString(shortBranch) +
      ", shortBranchComponent=" + toString(shortBranchComponent) +
      ", latestAnalysis=" + toString(latestAnalysis) +
      ", projectConfiguration=" + projectConfiguration +
      '}';
  }

  @CheckForNull
  private static String toString(@Nullable BranchDto shortBranch) {
    if (shortBranch == null) {
      return null;
    }
    return shortBranch.getBranchType() + ":" + shortBranch.getUuid() + ":" + shortBranch.getProjectUuid() + ":" + shortBranch.getMergeBranchUuid();
  }

  @CheckForNull
  private static String toString(@Nullable ComponentDto shortBranchComponent) {
    if (shortBranchComponent == null) {
      return null;
    }
    return shortBranchComponent.uuid() + ":" + shortBranchComponent.getKey();
  }

  @CheckForNull
  private static String toString(@Nullable SnapshotDto latestAnalysis) {
    if (latestAnalysis == null) {
      return null;
    }
    return latestAnalysis.getUuid() + ":" + latestAnalysis.getCreatedAt();
  }
}
