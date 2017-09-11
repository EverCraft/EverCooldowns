/*
 * This file is part of EverCooldowns.
 *
 * EverCooldowns is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * EverCooldowns is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with EverCooldowns.  If not, see <http://www.gnu.org/licenses/>.
 */
package fr.evercraft.evercooldowns;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import fr.evercraft.everapi.plugin.command.EParentCommand;
import fr.evercraft.evercooldowns.ECMessage.ECMessages;

public class ECCommand extends EParentCommand<EverCooldowns> {
	
	public ECCommand(final EverCooldowns plugin) {
        super(plugin, "evercooldowns", "cooldowns");
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(ECPermissions.EVERCOOLDOWNS.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return ECMessages.DESCRIPTION.getText();
	}

	@Override
	public boolean testPermissionHelp(final CommandSource source) {
		return source.hasPermission(ECPermissions.HELP.get());
	}
}
