/*
 * This file is part of EverMails.
 *
 * EverMails is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EverMails is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EverMails.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.evercraft.evermails;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import fr.evercraft.everapi.plugin.command.EParentCommand;
import fr.evercraft.evermails.EMMessage.EMMessages;

public class EMCommand extends EParentCommand<EverMails> {
	
	public EMCommand(final EverMails plugin) {
        super(plugin, "evermails");
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EMPermissions.EVERMAILS.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return EMMessages.DESCRIPTION.getText();
	}

	@Override
	public boolean testPermissionHelp(final CommandSource source) {
		return source.hasPermission(EMPermissions.HELP.get());
	}
}