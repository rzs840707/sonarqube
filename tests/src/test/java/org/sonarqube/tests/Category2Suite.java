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
package org.sonarqube.tests;

import com.sonar.orchestrator.Orchestrator;
import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.sonarqube.tests.component.BranchTest;
import org.sonarqube.tests.issue.AutoAssignTest;
import org.sonarqube.tests.issue.CommonRulesTest;
import org.sonarqube.tests.issue.CustomRulesTest;
import org.sonarqube.tests.issue.IssueActionTest;
import org.sonarqube.tests.issue.IssueBulkChangeTest;
import org.sonarqube.tests.issue.IssueChangelogTest;
import org.sonarqube.tests.issue.IssueCreationDateQPChangedTest;
import org.sonarqube.tests.issue.IssueCreationTest;
import org.sonarqube.tests.issue.IssueFilterExtensionTest;
import org.sonarqube.tests.issue.IssueFilterOnCommonRulesTest;
import org.sonarqube.tests.issue.IssueFilterTest;
import org.sonarqube.tests.issue.IssueMeasureTest;
import org.sonarqube.tests.issue.IssuePurgeTest;
import org.sonarqube.tests.issue.IssueSearchTest;
import org.sonarqube.tests.issue.IssueTrackingTest;
import org.sonarqube.tests.issue.IssueWorkflowTest;
import org.sonarqube.tests.issue.IssuesPageTest;
import org.sonarqube.tests.issue.NewIssuesMeasureTest;
import org.sonarqube.tests.rule.RulesPageTest;

import static util.ItUtils.pluginArtifact;
import static util.ItUtils.xooPlugin;

/**
 * @deprecated use dedicated suites in each package (see {@link org.sonarqube.tests.measure.MeasureSuite}
 * for instance)
 */
@Deprecated
@RunWith(Suite.class)
@Suite.SuiteClasses({
  // issue
  AutoAssignTest.class,
  CommonRulesTest.class,
  CustomRulesTest.class,
  IssueActionTest.class,
  IssueBulkChangeTest.class,
  IssueChangelogTest.class,
  IssueCreationTest.class,
  IssueFilterOnCommonRulesTest.class,
  IssueFilterTest.class,
  IssueFilterExtensionTest.class,
  IssueMeasureTest.class,
  IssuePurgeTest.class,
  IssueSearchTest.class,
  IssueTrackingTest.class,
  IssueWorkflowTest.class,
  NewIssuesMeasureTest.class,
  IssueCreationDateQPChangedTest.class,
  IssuesPageTest.class,
  // rule
  RulesPageTest.class,
  // branch
  BranchTest.class
})
public class Category2Suite {

  @ClassRule
  public static final Orchestrator ORCHESTRATOR = Orchestrator.builderEnv()
    .setServerProperty("sonar.search.httpPort", "9025")
    .addPlugin(xooPlugin())

    // issue
    .addPlugin(pluginArtifact("issue-filter-plugin"))

    // 1 second. Required for notification test.
    .setServerProperty("sonar.notifications.delay", "1")

    .setServerProperty("organization.enabled", "true")

    // reduce memory for Elasticsearch to 128M
    .setServerProperty("sonar.search.javaOpts", "-Xms128m -Xmx128m")

    .build();

}
