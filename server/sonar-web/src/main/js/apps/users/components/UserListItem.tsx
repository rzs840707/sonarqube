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
import * as React from 'react';
import Avatar from '../../../components/ui/Avatar';
import BulletListIcon from '../../../components/icons-components/BulletListIcon';
import { ButtonIcon } from '../../../components/ui/buttons';
import TokensForm from './TokensForm';
import UserActions from './UserActions';
import UserGroups from './UserGroups';
import UserScmAccounts from './UserScmAccounts';
import { IdentityProvider, User } from '../../../api/users';
import { translate } from '../../../helpers/l10n';
import { getBaseUrl } from '../../../helpers/urls';

interface Props {
  identityProvider?: IdentityProvider;
  isCurrentUser: boolean;
  onUpdateUsers: () => void;
  organizationsEnabled: boolean;
  user: User;
}

interface State {
  openTokenForm: boolean;
}

export default class UserListItem extends React.PureComponent<Props, State> {
  state: State = { openTokenForm: false };

  handleOpenTokensForm = () => this.setState({ openTokenForm: true });
  handleCloseTokensForm = () => this.setState({ openTokenForm: false });

  render() {
    const { identityProvider, onUpdateUsers, organizationsEnabled, user } = this.props;

    return (
      <tr>
        <td className="thin nowrap">
          <Avatar hash={user.avatar} name={user.name} size={36} />
        </td>
        <td>
          <div>
            <strong className="js-user-name">{user.name}</strong>
            <span className="js-user-login note little-spacer-left">{user.login}</span>
          </div>
          {user.email && <div className="js-user-email little-spacer-top">{user.email}</div>}
          {!user.local &&
            user.externalProvider !== 'sonarqube' && (
              <div className="js-user-identity-provider little-spacer-top">
                {identityProvider ? (
                  <div
                    className="identity-provider"
                    style={{ 'background-color': identityProvider.backgroundColor }}>
                    <img
                      alt={identityProvider.name}
                      src={getBaseUrl() + identityProvider.iconPath}
                      width="14"
                      height="14"
                    />
                    {user.externalIdentity}
                  </div>
                ) : (
                  <span>
                    {user.externalProvider}: {user.externalIdentity}
                  </span>
                )}
              </div>
            )}
        </td>
        <td>
          <UserScmAccounts scmAccounts={user.scmAccounts || []} />
        </td>
        {!organizationsEnabled && (
          <td>
            <UserGroups groups={user.groups || []} user={user} onUpdateUsers={onUpdateUsers} />
          </td>
        )}
        <td>
          {user.tokensCount}
          <ButtonIcon
            className="js-user-tokens spacer-left button-small"
            onClick={this.handleOpenTokensForm}
            tooltip={translate('users.update_tokens')}>
            <BulletListIcon />
          </ButtonIcon>
        </td>
        <td className="thin nowrap text-right">
          <UserActions
            isCurrentUser={this.props.isCurrentUser}
            onUpdateUsers={onUpdateUsers}
            user={user}
          />
        </td>
        {this.state.openTokenForm && (
          <TokensForm
            user={user}
            onClose={this.handleCloseTokensForm}
            onUpdateUsers={onUpdateUsers}
          />
        )}
      </tr>
    );
  }
}
