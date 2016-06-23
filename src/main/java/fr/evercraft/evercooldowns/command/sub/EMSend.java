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

public class EMSend extends ESubCommand<EverMails> {
	public EMSend(final EverMails plugin, final EMCommand command) {
        super(plugin, command, "send");
    }
	
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(EMPermissions.SEND.get());
	}

	public Text description(final CommandSource source) {
		return EMMessages.SEND_DESCRIPTION.getText();
	}
	
	public List<String> subTabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		List<String> suggests = new ArrayList<String>();
		if(args.size() == 1) {
			suggests.addAll(this.plugin.getService().getMails().keySet());
		} else if(args.size() == 2) {
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
			String identifier = args.get(1);
			args.remove(0);
			return commandSend(source, identifier, String.join(" ", args));
		}
		source.sendMessage(this.help(source));
		return false;
	}

	private boolean commandSend(CommandSource player, String identifier, String message) {
		String address = this.plugin.getService().getMails().get(identifier);
		// Adresse mail connu
		if(address != null) {
			// Mail envoyé
			if(this.plugin.getService().send(
					address,
					EMMessages.SEND_OBJECT.get()
					.replaceAll("<player>", player.getName()), 
					EMMessages.SEND_MESSAGE.get()
						.replaceAll("<player>", player.getName())
						.replaceAll("<message>", message))) {
				// Joueur identique
				if(player.getName().equalsIgnoreCase(identifier)) {
					player.sendMessage(EChat.of(EMMessages.PREFIX.get() + EMMessages.SEND_EQUALS.get()
							.replaceAll("<player>", identifier)
							.replaceAll("<address>", address)
							.replaceAll("<message>", message)));
				// Joueur différent
				} else {
					player.sendMessage(EChat.of(EMMessages.PREFIX.get() + EMMessages.SEND_PLAYER.get()
							.replaceAll("<player>", identifier)
							.replaceAll("<address>", address)
							.replaceAll("<message>", message)));
				}
				return true;
			// Erreur lors de l'envoie
			} else {
				player.sendMessage(EChat.of(EMMessages.PREFIX.get() + EAMessages.COMMAND_ERROR.get()));
			}
		// Aucune adresse mail connu
		} else {
			player.sendMessage(EChat.of(EMMessages.PREFIX.get() + EMMessages.SEND_ERROR.get()
					.replaceAll("<player>", identifier)
					.replaceAll("<message>", message)));
		}
		return false;
	}
}
