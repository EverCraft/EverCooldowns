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

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.EChat;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.evermails.EMCommand;
import fr.evercraft.evermails.EMMessage.EMMessages;
import fr.evercraft.evermails.EMPermissions;
import fr.evercraft.evermails.EverMails;

public class EMAlert extends ESubCommand<EverMails> {
	public EMAlert(final EverMails plugin, final EMCommand command) {
        super(plugin, command, "alert");
    }
	
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EMPermissions.ALERT.get());
	}

	public Text description(final CommandSource source) {
		return EMMessages.ALERT_DESCRIPTION.getText();
	}
	
	public List<String> subTabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		List<String> suggests = new ArrayList<String>();
		if(args.size() == 1) {
			suggests.add("Message...");
		}
		return suggests;
	}

	public Text help(final CommandSource source) {
		return Text.builder("/" + this.getName() + " <" + EAMessages.ARGS_MESSAGE.get() + ">")
				.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
				.color(TextColors.RED)
				.build();
	}
	
	public boolean subExecute(final CommandSource source, final List<String> args) {
		if(args.size() == 2) {
			return commandAlert(source, String.join(" ", args));
		}
		source.sendMessage(this.help(source));
		return false;
	}

	private boolean commandAlert(CommandSource player, String message) {
		// Des adresses sont enregistré
		if(!this.plugin.getService().getMails().isEmpty()) {
			// Mail envoyé
			if(this.plugin.getService().alert(
					EMMessages.ALERT_OBJECT.get()
						.replaceAll("<player>", player.getName()), 
					EMMessages.ALERT_MESSAGE.get()
						.replaceAll("<player>", player.getName())
						.replaceAll("<message>", message))) {
				
				player.sendMessage(EChat.of(EMMessages.PREFIX.get() + EMMessages.ALERT_PLAYER.get()
						.replaceAll("<message>", message)));
				return true;
			// Erreur lors de l'envoie
			} else {
				player.sendMessage(EChat.of(EMMessages.PREFIX.get() + EAMessages.COMMAND_ERROR.get()));
			}
		// Aucune adresse mail
		} else {
			player.sendMessage(EChat.of(EMMessages.PREFIX.get() + EMMessages.ALERT_ERROR.get()));
		}
		return false;
	}
	
}
