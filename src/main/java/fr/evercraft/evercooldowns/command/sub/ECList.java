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
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
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

public class ECList extends ESubCommand<EverCooldowns> {
	public ECList(final EverCooldowns plugin, final ECCommand command) {
        super(plugin, command, "list");
    }
	
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(ECPermissions.LIST.get());
	}

	public Text description(final CommandSource source) {
		return ECMessages.LIST_DESCRIPTION.getText();
	}
	
	public Collection<String> tabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		if (args.size() == 1 && source.hasPermission(ECPermissions.LIST_OTHERS.get())) {
			return this.getAllPlayers(source, true);
		}
		return Arrays.asList();
	}
	
	public Text help(final CommandSource source) {
		if (source.hasPermission(ECPermissions.LIST_OTHERS.get())) {
			return Text.builder("/" + this.getName() + " <" + EAMessages.ARGS_PLAYER.getString() + ">")
					.onClick(TextActions.suggestCommand("/" + this.getName() + " "))
					.color(TextColors.RED)
					.build();
		} 
		return Text.builder("/" + this.getName())
				.onClick(TextActions.suggestCommand("/" + this.getName()))
				.color(TextColors.RED)
				.build();
	}
	
	public CompletableFuture<Boolean> execute(final CommandSource source, final List<String> args) {
		if (args.size() == 0) {
			// Si la source est un joueur
			if (source instanceof EPlayer) {
				return this.commandList((EPlayer) source);
			// La source n'est pas un joueur
			} else {
				EAMessages.COMMAND_ERROR_FOR_PLAYER.sendTo(source);
			}
		} else if (args.size() == 1) {
			Optional<User> optUser = this.plugin.getEServer().getUser(args.get(0));
			// Le joueur existe
			if (optUser.isPresent()){
				return this.commandList(source, optUser.get());
			// Le joueur est introuvable
			} else {
				EAMessages.PLAYER_NOT_FOUND.sender()
					.prefix(ECMessages.PREFIX)
					.replace("{player}", args.get(0))
					.sendTo(source);
			}
		} else {
			source.sendMessage(this.help(source));
		}
		return CompletableFuture.completedFuture(false);
	}

	private CompletableFuture<Boolean> commandList(final EPlayer player) {
		Map<String, Long> cooldowns = player.getCooldowns();
		if (!cooldowns.isEmpty()) {
			List<Text> lists = new ArrayList<Text>();
			if (player.hasPermission(ECPermissions.REMOVE.get())) {
				for (Entry<String, Long> cooldown : cooldowns.entrySet()) {
					lists.add(ECMessages.LIST_PLAYER_LINE.getFormat()
							.toText("{cooldown}", () -> cooldown.getKey(), 
									"{time}", () -> this.plugin.getEverAPI().getManagerUtils().getDate().formatDate(cooldown.getValue()),
									"{delete}", () -> this.getButtonDelete(player, cooldown.getKey())));
				}
			} else {
				for (Entry<String, Long> cooldown : cooldowns.entrySet()) {
					lists.add(ECMessages.LIST_PLAYER_LINE.getFormat()
							.toText("{cooldown}", () -> cooldown.getKey(),
									"{time}", () -> this.plugin.getEverAPI().getManagerUtils().getDate().formatDate(cooldown.getValue())));
				}
			}
			this.plugin.getEverAPI().getManagerService().getEPagination().sendTo(ECMessages.LIST_PLAYER_TITLE.getText().toBuilder()
					.onClick(TextActions.runCommand(this.getName())).build(), lists, player);
		} else {
			ECMessages.LIST_PLAYER_EMPTY.sendTo(player);
		}
		return CompletableFuture.completedFuture(true);
	}
	
	private CompletableFuture<Boolean> commandList(final CommandSource staff, final User user) {
		if (staff.getIdentifier().equals(user.getIdentifier()) && staff instanceof EPlayer) {
			return this.commandList((EPlayer) staff);
		} else {
			this.plugin.getThreadAsync().execute(() -> this.commandListAsync(staff, user));
		}
		return CompletableFuture.completedFuture(true);
	}
	
	private void commandListAsync(final CommandSource staff, final User user) {
		Optional<CooldownsSubject> optSubject = this.plugin.getService().get(user.getUniqueId());
		if (optSubject.isPresent()) {
			CooldownsSubject subject = optSubject.get();
			Map<String, Long> cooldowns = subject.getAll();
			if (!cooldowns.isEmpty()) {
				List<Text> lists = new ArrayList<Text>();
				if (staff.hasPermission(ECPermissions.REMOVE.get()) && staff.hasPermission(ECPermissions.REMOVE_OTHERS.get())) {
					for (Entry<String, Long> cooldown : cooldowns.entrySet()) {
						lists.add(ECMessages.LIST_STAFF_LINE.getFormat()
								.toText("{cooldown}", () -> cooldown.getKey(),
										"{time}", () -> this.plugin.getEverAPI().getManagerUtils().getDate().formatDate(cooldown.getValue()),
										"{delete}", () -> this.getButtonDeleteOthers(user, cooldown.getKey())));
					}
				} else {
					for (Entry<String, Long> cooldown : cooldowns.entrySet()) {
						lists.add(ECMessages.LIST_STAFF_LINE.getFormat()
								.toText("{cooldown}", () -> cooldown.getKey(),
										"{time}", () -> this.plugin.getEverAPI().getManagerUtils().getDate().formatDate(cooldown.getValue())));
					}
				}
				this.plugin.getEverAPI().getManagerService().getEPagination().sendTo(ECMessages.LIST_STAFF_TITLE.getText().toBuilder()
						.onClick(TextActions.runCommand(this.getName())).build(), lists, staff);
			} else {
				ECMessages.LIST_STAFF_EMPTY.sender()
					.replace("{staff}", staff.getName())
					.replace("{player}", user.getName())
					.sendTo(staff);
			}
		} else {
			EAMessages.PLAYER_NOT_FOUND.sender()
				.prefix(ECMessages.PREFIX)
				.sendTo(staff);
		}
	}
	
	public Text getButtonDelete(final EPlayer player, final String cooldown){
		return ECMessages.LIST_PLAYER_DELETE.getText().toBuilder()
					.onHover(TextActions.showText(ECMessages.LIST_PLAYER_DELETE_HOVER.getFormat()
							.toText("{player}", player.getName(),
									"{cooldown}", cooldown)))
					.onClick(TextActions.runCommand("/" + this.getParentName() + " remove " + cooldown))
					.build();
	}
	
	public Text getButtonDeleteOthers(final User player, final String cooldown){
		return ECMessages.LIST_STAFF_DELETE.getText().toBuilder()
					.onHover(TextActions.showText(ECMessages.LIST_STAFF_DELETE_HOVER.getFormat()
							.toText("{player}", player.getName(),
									"{cooldown}", cooldown)))
					.onClick(TextActions.runCommand("/" + this.getParentName() + " remove " + cooldown + " " + player.getName()))
					.build();
	}
}
