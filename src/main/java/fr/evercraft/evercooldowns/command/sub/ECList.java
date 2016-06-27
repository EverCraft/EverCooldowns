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
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import fr.evercraft.everapi.EAMessage.EAMessages;
import fr.evercraft.everapi.plugin.EChat;
import fr.evercraft.everapi.plugin.command.ESubCommand;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.cooldown.CooldownsSubject;
import fr.evercraft.everapi.text.ETextBuilder;
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
	
	public List<String> subTabCompleter(final CommandSource source, final List<String> args) throws CommandException {
		if(args.size() == 1 && source.hasPermission(ECPermissions.LIST_OTHERS.get())) {
			return null;
		}
		return new ArrayList<String>();
	}
	
	public Text help(final CommandSource source) {
		if(source.hasPermission(ECPermissions.LIST_OTHERS.get())) {
			return Text.builder("/" + this.getName() + " <" + EAMessages.ARGS_PLAYER.get() + ">")
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
		
		if(args.size() == 0) {
			// Si la source est un joueur
			if(source instanceof EPlayer) {
				resultat = this.commandList((EPlayer) source);
			// La source n'est pas un joueur
			} else {
				source.sendMessage(EAMessages.COMMAND_ERROR_FOR_PLAYER.getText());
			}
		} else if(args.size() == 1) {
			Optional<User> optUser = this.plugin.getEServer().getUser(args.get(0));
			// Le joueur existe
			if(optUser.isPresent()){
				resultat = this.commandList(source, optUser.get());
			// Le joueur est introuvable
			} else {
				source.sendMessage(EChat.of(ECMessages.PREFIX.get() + EAMessages.PLAYER_NOT_FOUND.get()));
			}
		} else {
			source.sendMessage(this.help(source));
		}
		return resultat;
	}

	private boolean commandList(final EPlayer player) {
		Map<String, Long> cooldowns = player.getCooldowns();
		if(!cooldowns.isEmpty()) {
			List<Text> lists = new ArrayList<Text>();
			if(player.hasPermission(ECPermissions.REMOVE.get())) {
				for (Entry<String, Long> cooldown : cooldowns.entrySet()) {
					lists.add(ETextBuilder.toBuilder(ECMessages.LIST_PLAYER_LINE.get()
							.replaceAll("<cooldown>", cooldown.getKey())
							.replaceAll("<time>", this.plugin.getEverAPI().getManagerUtils().getDate().formatDateDiff(cooldown.getValue())))
						.replace("<delete>", this.getButtonDelete(player, cooldown.getKey()))
						.build());
				}
			} else {
				for (Entry<String, Long> cooldown : cooldowns.entrySet()) {
					lists.add(EChat.of(ECMessages.LIST_PLAYER_LINE.get()
							.replaceAll("<cooldown>", cooldown.getKey())
							.replaceAll("<time>", this.plugin.getEverAPI().getManagerUtils().getDate().formatDateDiff(cooldown.getValue()))));
				}
			}
			this.plugin.getEverAPI().getManagerService().getEPagination().sendTo(ECMessages.LIST_PLAYER_TITLE.getText().toBuilder()
					.onClick(TextActions.runCommand(this.getName())).build(), lists, player);
		} else {
			player.sendMessage(ECMessages.PREFIX.getText().concat(ECMessages.LIST_PLAYER_EMPTY.getText()));
		}
		return false;
	}
	
	private boolean commandList(final CommandSource staff, final User user) {
		if(staff.getIdentifier().equals(user.getIdentifier()) && staff instanceof EPlayer) {
			return this.commandList((EPlayer) staff);
		} else {
			this.plugin.getThreadAsync().execute(() -> this.commandListAsync(staff, user));
		}
		return false;
	}
	
	private void commandListAsync(final CommandSource staff, final User user) {
		Optional<CooldownsSubject> optSubject = this.plugin.getService().get(user.getUniqueId());
		if(optSubject.isPresent()) {
			CooldownsSubject subject = optSubject.get();
			Map<String, Long> cooldowns = subject.getAll();
			if(!cooldowns.isEmpty()) {
				List<Text> lists = new ArrayList<Text>();
				if(staff.hasPermission(ECPermissions.REMOVE.get()) && staff.hasPermission(ECPermissions.REMOVE_OTHERS.get())) {
					for (Entry<String, Long> cooldown : cooldowns.entrySet()) {
						lists.add(ETextBuilder.toBuilder(ECMessages.LIST_STAFF_LINE.get()
								.replaceAll("<cooldown>", cooldown.getKey())
								.replaceAll("<time>", this.plugin.getEverAPI().getManagerUtils().getDate().formatDateDiff(cooldown.getValue())))
							.replace("<delete>", this.getButtonDeleteOthers(user, cooldown.getKey()))
							.build());
					}
				} else {
					for (Entry<String, Long> cooldown : cooldowns.entrySet()) {
						lists.add(EChat.of(ECMessages.LIST_STAFF_LINE.get()
								.replaceAll("<cooldown>", cooldown.getKey())
								.replaceAll("<time>", this.plugin.getEverAPI().getManagerUtils().getDate().formatDateDiff(cooldown.getValue()))));
					}
				}
				this.plugin.getEverAPI().getManagerService().getEPagination().sendTo(ECMessages.LIST_STAFF_TITLE.getText().toBuilder()
						.onClick(TextActions.runCommand(this.getName())).build(), lists, staff);
			} else {
				staff.sendMessage(EChat.of(ECMessages.PREFIX.get() + ECMessages.LIST_STAFF_EMPTY.get()
						.replaceAll("<staff>", staff.getName())
						.replaceAll("<player>", user.getName())));
			}
		} else {
			staff.sendMessage(EChat.of(ECMessages.PREFIX.get() + EAMessages.PLAYER_NOT_FOUND.get()));
		}
	}
	
	public Text getButtonDelete(final EPlayer player, final String cooldown){
		return ECMessages.LIST_PLAYER_DELETE.getText().toBuilder()
					.onHover(TextActions.showText(EChat.of(ECMessages.LIST_PLAYER_DELETE_HOVER.get()
							.replaceAll("<player>", player.getName())
							.replaceAll("<cooldown>", cooldown))))
					.onClick(TextActions.runCommand("/" + this.getParentName() + " remove " + cooldown))
					.build();
	}
	
	public Text getButtonDeleteOthers(final User player, final String cooldown){
		return ECMessages.LIST_STAFF_DELETE.getText().toBuilder()
					.onHover(TextActions.showText(EChat.of(ECMessages.LIST_STAFF_DELETE_HOVER.get()
							.replaceAll("<player>", player.getName())
							.replaceAll("<cooldown>", cooldown))))
					.onClick(TextActions.runCommand("/" + this.getParentName() + " remove " + cooldown + " " + player.getName()))
					.build();
	}
}
