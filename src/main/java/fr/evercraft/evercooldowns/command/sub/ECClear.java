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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.cooldown.CooldownsSubject;
import fr.evercraft.evercooldowns.ECCommand;
import fr.evercraft.evercooldowns.ECMessage.ECMessages;
import fr.evercraft.evercooldowns.ECPermissions;
import fr.evercraft.evercooldowns.EverCooldowns;

public class ECClear extends ESubCommand<EverCooldowns> {
	public ECClear(final EverCooldowns plugin, final ECCommand command) {
        super(plugin, command, "clear");
    }
	
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(ECPermissions.CLEAR.get());
	}

	public Text description(final CommandSource source) {
		return ECMessages.CLEAR_DESCRIPTION.getText();
	}
	
	public Collection<String> subTabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		if (args.size() == 1 && source.hasPermission(ECPermissions.CLEAR_OTHERS.get())) {
			return this.getAllPlayers(source, true);
		}
		return Arrays.asList();
	}

	public Text help(final CommandSource source) {
		if (source.hasPermission(ECPermissions.CLEAR_OTHERS.get())) {
			return Text.builder("/" + this.getName() + " <" + EAMessages.ARGS_PLAYER.getString() + "|*>")
					.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
					.color(TextColors.RED)
					.build();
		} 
		return Text.builder("/" + this.getName())
				.onClick(TextActions.suggestCommand("/" + this.getName()))
				.color(TextColors.RED)
				.build();
	}
	
	public boolean subExecute(final CommandSource source, final List<String> args) {
		boolean resultat = false;
		
		if (args.size() == 0) {
			// Si la source est un joueur
			if (source instanceof EPlayer) {
				resultat = this.commandClear((EPlayer) source);
			// La source n'est pas un joueur
			} else {
				EAMessages.COMMAND_ERROR_FOR_PLAYER.sendTo(source);
			}
		} else if (args.size() == 1) {
			if (source.hasPermission(ECPermissions.CLEAR_OTHERS.get())) {
				if (args.get(0).equalsIgnoreCase("*") || args.get(0).equalsIgnoreCase("all")) {
					resultat = this.commandClearAll(source);
				} else {
					Optional<User> optUser = this.plugin.getEServer().getUser(args.get(0));
					// Le joueur existe
					if (optUser.isPresent()){
						resultat = this.commandClear(source, optUser.get());
					// Le joueur est introuvable
					} else {
						EAMessages.PLAYER_NOT_FOUND.sender()
							.prefix(ECMessages.PREFIX)
							.sendTo(source);
					}
				}
			} else {
				EAMessages.NO_PERMISSION.sender()
					.prefix(ECMessages.PREFIX)
					.sendTo(source);
			}
		} else {
			source.sendMessage(this.help(source));
		}
		return resultat;
	}

	private boolean commandClear(final EPlayer player) {
		if (player.clearCooldown()) {
			ECMessages.CLEAR_EQUALS.sendTo(player);
		} else {
			ECMessages.CLEAR_ERROR_PLAYER.sendTo(player);
		}
		return true;
	}
	
	private boolean commandClearAll(final CommandSource player) {
		this.plugin.getDataBases().clearAll();
		ECMessages.CLEAR_ALL.sendTo(player);
		return true;
	}
	
	private boolean commandClear(final CommandSource staff, final User user) {
		if (staff.getIdentifier().equals(user.getIdentifier()) && staff instanceof EPlayer) {
			return this.commandClear((EPlayer) staff);
		} else {
			this.plugin.getThreadAsync().execute(() -> this.commandClearAsync(staff, user));
		}
		return true;
	}
	
	private void commandClearAsync(final CommandSource staff, final User user) {
		Optional<CooldownsSubject> optSubject = this.plugin.getService().get(user.getUniqueId());
		if (optSubject.isPresent()) {
			CooldownsSubject subject = optSubject.get();
			if (subject.clear()) {
				ECMessages.CLEAR_STAFF.sender()
					.replace("<staff>", staff.getName())
					.replace("<player>", user.getName())
					.sendTo(staff);
				user.getPlayer().ifPresent(player ->
					ECMessages.CLEAR_PLAYER.sender()
						.replace("<staff>", staff.getName())
						.replace("<player>", user.getName())
						.sendTo(player)
				);
			} else {
				ECMessages.CLEAR_ERROR_STAFF.sender()
					.replace("<staff>", staff.getName())
					.replace("<player>", user.getName())
					.sendTo(staff);
			}
		} else {
			EAMessages.PLAYER_NOT_FOUND.sender()
				.prefix(ECMessages.PREFIX)
				.replace("<staff>", staff.getName())
				.replace("<player>", user.getName())
				.sendTo(staff);
		}
	}
}
