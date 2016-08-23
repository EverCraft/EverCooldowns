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
package fr.evercraft.evercooldowns.command.sub;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.EChat;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.cooldown.CooldownsSubject;
import fr.evercraft.evercooldowns.ECCommand;
import fr.evercraft.evercooldowns.ECMessage.ECMessages;
import fr.evercraft.evercooldowns.ECPermissions;
import fr.evercraft.evercooldowns.EverCooldowns;

public class ECRemove extends ESubCommand<EverCooldowns> {
	public ECRemove(final EverCooldowns plugin, final ECCommand command) {
        super(plugin, command, "remove");
    }
	
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(ECPermissions.REMOVE.get());
	}

	public Text description(final CommandSource source) {
		return ECMessages.REMOVE_DESCRIPTION.getText();
	}
	
	public List<String> subTabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		List<String> suggest = new ArrayList<String>();
		if (args.size() == 1) {
			suggest.add(EAMessages.ARGS_COOLDOWN.get());
		} else if (args.size() == 2) {
			suggest = null;
		}
		return suggest;
	}

	public Text help(final CommandSource source) {
		if (source.hasPermission(ECPermissions.REMOVE_OTHERS.get())) {
			return Text.builder("/" + this.getName() + " <" + EAMessages.ARGS_COOLDOWN.get() + "> <" + EAMessages.ARGS_PLAYER.get() + ">")
					.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
					.color(TextColors.RED)
					.build();
		} 
		return Text.builder("/" + this.getName() + " <" + EAMessages.ARGS_COOLDOWN.get() + ">")
				.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
				.color(TextColors.RED)
				.build();
	}
	
	public boolean subExecute(final CommandSource source, final List<String> args) {
		boolean resultat = false;
		
		if (args.size() == 1) {
			// Si la source est un joueur
			if (source instanceof EPlayer) {
				resultat = this.commandRemove((EPlayer) source, args.get(0));
			// La source n'est pas un joueur
			} else {
				source.sendMessage(EAMessages.COMMAND_ERROR_FOR_PLAYER.getText());
			}
		} else if (args.size() == 2) {
			Optional<User> optUser = this.plugin.getEServer().getUser(args.get(1));
			// Le joueur existe
			if (optUser.isPresent()){
				resultat = this.commandRemove(source, optUser.get(), args.get(0));
			// Le joueur est introuvable
			} else {
				source.sendMessage(EChat.of(ECMessages.PREFIX.get() + EAMessages.PLAYER_NOT_FOUND.get()));
			}
		} else {
			source.sendMessage(this.help(source));
		}
		return resultat;
	}

	private boolean commandRemove(final EPlayer player, final String cooldown) {
		if (player.removeCooldown(cooldown)) {
			player.sendMessage(EChat.of(ECMessages.PREFIX.get() + ECMessages.REMOVE_EQUALS.get()
					.replaceAll("<cooldown>", cooldown)));
		} else {
			player.sendMessage(EChat.of(ECMessages.PREFIX.get() + ECMessages.REMOVE_ERROR_PLAYER.get()
					.replaceAll("<cooldown>", cooldown)));
		}
		return true;
	}
	
	private boolean commandRemove(final CommandSource staff, final User user, final String cooldown) {
		if (staff.getIdentifier().equals(user.getIdentifier()) && staff instanceof EPlayer) {
			return this.commandRemove((EPlayer) staff, cooldown);
		} else {
			this.plugin.getThreadAsync().execute(() -> this.commandRemoveAsync(staff, user, cooldown));
		}
		return true;
	}
	
	private void commandRemoveAsync(final CommandSource staff, final User user, final String cooldown) {
		Optional<CooldownsSubject> optSubject = this.plugin.getService().get(user.getUniqueId());
		if (optSubject.isPresent()) {
			CooldownsSubject subject = optSubject.get();
			if (subject.remove(cooldown)) {
				staff.sendMessage(EChat.of(ECMessages.PREFIX.get() + ECMessages.REMOVE_STAFF.get()
						.replaceAll("<staff>", staff.getName())
						.replaceAll("<player>", user.getName())
						.replaceAll("<cooldown>", cooldown)));
				Optional<Player> player = user.getPlayer();
				if (player.isPresent()) {
					player.get().sendMessage(EChat.of(ECMessages.PREFIX.get() + ECMessages.REMOVE_PLAYER.get()
							.replaceAll("<staff>", staff.getName())
							.replaceAll("<player>", user.getName())
							.replaceAll("<cooldown>", cooldown)));
				}
			} else {
				staff.sendMessage(EChat.of(ECMessages.PREFIX.get() + ECMessages.REMOVE_ERROR_STAFF.get()
						.replaceAll("<staff>", staff.getName())
						.replaceAll("<player>", user.getName())
						.replaceAll("<cooldown>", cooldown)));
			}
		} else {
			staff.sendMessage(EChat.of(ECMessages.PREFIX.get() + EAMessages.PLAYER_NOT_FOUND.get()));
		}
	}
}
