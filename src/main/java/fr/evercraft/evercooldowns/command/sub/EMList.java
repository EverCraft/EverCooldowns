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
package fr.evercraft.evermails.command.sub;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import fr.evercraft.everapi.plugin.EChat;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.evermails.EMCommand;
import fr.evercraft.evermails.EMMessage.EMMessages;
import fr.evercraft.evermails.EMPermissions;
import fr.evercraft.evermails.EverMails;

public class EMList extends ESubCommand<EverMails> {
	public EMList(final EverMails plugin, final EMCommand command) {
        super(plugin, command, "list");
    }
	
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EMPermissions.LIST.get());
	}

	public Text description(final CommandSource source) {
		return EMMessages.LIST_DESCRIPTION.getText();
	}
	
	public List<String> subTabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		return new ArrayList<String>();
	}

	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName())
					.onClick(TextActions.suggestCommand("/" + this.getName()))
					.color(TextColors.RED)
					.build();
	}
	
	public boolean subExecute(final CommandSource source, final List<String> args) {
		if(args.size() == 0) {
			return commandList(source);
		}
		source.sendMessage(this.help(source));
		return false;
	}

	private boolean commandList(final CommandSource player) {
		List<Text> list = new ArrayList<Text>();
		
		// Aucune adresse mail
		if(this.plugin.getService().getMails().isEmpty()) {
			list.add(EMMessages.LIST_EMPTY.getText());
		// Des adresses sont enregistré 
		} else {
			for (Entry<String, String> mail : this.plugin.getService().getMails().entrySet()) {
				list.add(EChat.of(EMMessages.LIST_LINE.get()
						.replaceAll("<player>", mail.getKey())
						.replaceAll("<address>", mail.getValue())));
			}
		}
		
		this.plugin.getEverAPI().getManagerService().getEPagination().sendTo(
				EMMessages.LIST_TITLE.getText(), 
				list, player);
		return true;
	}
}
