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
import java.util.concurrent.CompletableFuture;

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
	
	public Collection<String> tabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		if (args.size() == 1) {
			// TODO
			return Arrays.asList("cooldown...");
		} else if (args.size() == 2) {
			return this.getAllUsers(args.get(0), source);
		}
		return Arrays.asList();
	}

	public Text help(final CommandSource source) {
		if (source.hasPermission(ECPermissions.REMOVE_OTHERS.get())) {
			return Text.builder("/" + this.getName() + " <" + EAMessages.ARGS_COOLDOWN.getString() + "> [" + EAMessages.ARGS_USER.getString() + "]")
					.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
					.color(TextColors.RED)
					.build();
		} 
		return Text.builder("/" + this.getName() + " <" + EAMessages.ARGS_COOLDOWN.getString() + ">")
				.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
				.color(TextColors.RED)
				.build();
	}
	
	public CompletableFuture<Boolean> execute(final CommandSource source, final List<String> args) {
		if (args.size() == 1) {
			// Si la source est un joueur
			if (source instanceof EPlayer) {
				return this.commandRemove((EPlayer) source, args.get(0));
			// La source n'est pas un joueur
			} else {
				source.sendMessage(EAMessages.COMMAND_ERROR_FOR_PLAYER.getText());
			}
		} else if (args.size() == 2) {
			Optional<User> optUser = this.plugin.getEServer().getUser(args.get(1));
			// Le joueur existe
			if (optUser.isPresent()){
				return this.commandRemove(source, optUser.get(), args.get(0));
			// Le joueur est introuvable
			} else {
				EAMessages.PLAYER_NOT_FOUND.sender()
					.prefix(ECMessages.PREFIX)
					.sendTo(source);
			}
		} else {
			source.sendMessage(this.help(source));
		}
		return CompletableFuture.completedFuture(false);
	}

	private CompletableFuture<Boolean> commandRemove(final EPlayer player, final String cooldown) {
		if (player.removeCooldown(cooldown)) {
			ECMessages.REMOVE_EQUALS.sender()
				.replace("<cooldown>", cooldown)
				.sendTo(player);
		} else {
			ECMessages.REMOVE_ERROR_PLAYER.sender()
				.replace("<cooldown>", cooldown)
				.sendTo(player);
		}
		return CompletableFuture.completedFuture(true);
	}
	
	private CompletableFuture<Boolean> commandRemove(final CommandSource staff, final User user, final String cooldown) {
		if (staff.getIdentifier().equals(user.getIdentifier()) && staff instanceof EPlayer) {
			return this.commandRemove((EPlayer) staff, cooldown);
		} else {
			this.plugin.getThreadAsync().execute(() -> this.commandRemoveAsync(staff, user, cooldown));
		}
		return CompletableFuture.completedFuture(true);
	}
	
	private void commandRemoveAsync(final CommandSource staff, final User user, final String cooldown) {
		Optional<CooldownsSubject> optSubject = this.plugin.getService().get(user.getUniqueId());
		if (optSubject.isPresent()) {
			CooldownsSubject subject = optSubject.get();
			if (subject.remove(cooldown)) {
				ECMessages.REMOVE_ERROR_PLAYER.sender()
					.replace("<staff>", staff.getName())
					.replace("<player>", user.getName())
					.replace("<cooldown>", cooldown)
					.sendTo(staff);
				user.getPlayer().ifPresent(player -> ECMessages.REMOVE_PLAYER.sender()
						.replace("<staff>", staff.getName())
						.replace("<player>", user.getName())
						.replace("<cooldown>", cooldown)
						.sendTo(player));
			} else {
				ECMessages.REMOVE_ERROR_STAFF.sender()
					.replace("<staff>", staff.getName())
					.replace("<player>", user.getName())
					.replace("<cooldown>", cooldown)
					.sendTo(staff);
			}
		} else {
			EAMessages.PLAYER_NOT_FOUND.sender()
				.prefix(ECMessages.PREFIX)
				.sendTo(staff);
		}
	}
}
