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
package fr.evercraft.evercooldowns.service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.reflect.TypeToken;

import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import fr.evercraft.everapi.java.UtilsMap;
import fr.evercraft.everapi.plugin.file.EConfig;
import fr.evercraft.everapi.services.cooldown.CooldownsService;
import fr.evercraft.evercooldowns.EverCooldowns;

public class ECooldownsConfig extends EConfig<EverCooldowns> {

	public ECooldownsConfig(final EverCooldowns plugin) {
		super(plugin, "cooldowns");
	}
	
	@Override
	public void loadDefault() {
		if (this.getNode().getValue() == null) {
			// Heal
			CommentedConfigurationNode heal_config = this.get("heal_group");
			heal_config.getNode("commands").setValue(Arrays.asList("heal", "food"));
			
			HashMap<String, Long> heal_permissions = new HashMap<String, Long>();
			heal_permissions.put("default", 5L);
			heal_permissions.put("moderator", 1L);
			heal_config.getNode("permissions").setValue(heal_permissions);
			
			// Lag
			CommentedConfigurationNode lag_group = this.get("lag_group");
			lag_group.getNode("command").setValue("lag");
			
			HashMap<String, Long> lag_permissions = new HashMap<String, Long>();
			lag_permissions.put("default", 10L);
			lag_permissions.put("moderator", 5L);
			lag_group.getNode("permissions").setValue(lag_permissions);
			
			// Ping
			CommentedConfigurationNode ping_group = this.get("ping");
			
			HashMap<String, Long> ping_permissions = new HashMap<String, Long>();
			ping_permissions.put("default", 2L);
			ping_permissions.put("moderator", 1L);
			ping_group.setValue(ping_permissions);
			
			// Default
			CommentedConfigurationNode default_group = this.get("default");
			
			HashMap<String, Long> default_permissions = new HashMap<String, Long>();
			default_permissions.put("default", 1L);
			default_permissions.put("admin", 0L);
			default_group.setValue(default_permissions);
		} else {
			addDefault("default.default", 0);
		}
	}
	
	public Map<String, EValue> getCooldowns() {
		Map<String, EValue> commands = new HashMap<String, EValue>();
		
		for (Entry<Object, ? extends ConfigurationNode> command : this.getNode().getChildrenMap().entrySet()) {
			if (command.getKey() instanceof String) {
				String group = (String) command.getKey();
				ConfigurationNode config_commands = command.getValue().getNode("commands");
				ConfigurationNode config_command = command.getValue().getNode("command");
				ConfigurationNode config_permissions = command.getValue().getNode("permissions");
				
				// Goupe
				if ((!config_commands.isVirtual() || !config_command.isVirtual()) && !config_permissions.isVirtual()) {
					Map<String, Long> cooldowns = new HashMap<String, Long>();
					// Liste des permissions
					for (Entry<Object, ? extends ConfigurationNode> cooldown : config_permissions.getChildrenMap().entrySet()) {
						if (cooldown.getKey() instanceof String) {
							String permission = (String) cooldown.getKey();
							if (!permission.equalsIgnoreCase(CooldownsService.NAME_DEFAULT)) {
								long value = cooldown.getValue().getLong(-1L);
								if (value >= 0) {
									cooldowns.put((String) cooldown.getKey(), value*1000);
								} else {
									this.plugin.getELogger().warn("The value of the cooldown is invalid : (group='" + group + "';permission='" + permission + "';value='" + value + "')");
								}
							}
						} else {
							this.plugin.getELogger().warn("The name of the permission is invalid : (group='" + group + "';permission='" + cooldown.getKey().toString() + "')");
						}
					}
					
					EValue value = new EValue(config_permissions.getNode(CooldownsService.NAME_DEFAULT).getLong(0)*1000, UtilsMap.valueLinkedASC(cooldowns));
					if (config_commands.isVirtual()) {
						String name = config_command.getString("");
						if (!name.isEmpty()) {
							if (!commands.containsKey(name)) {
								commands.put(name, value);
							} else {
								this.plugin.getELogger().warn("The name is already used : (group='" + group + "';name='" + name + "')");
							}
						} else {
							this.plugin.getELogger().warn("The command name is empty : (group='" + group + "')");
						}
					} else {
						try {
							for (String name : config_commands.getList(TypeToken.of(String.class))) {
								if (!name.isEmpty()) {
									if (!commands.containsKey(name)) {
										commands.put(name, value);
									} else {
										this.plugin.getELogger().warn("The name is already used : (group='" + group + "';name='" + name + "')");
									}
								} else {
									this.plugin.getELogger().warn("The name of a command is empty : (group='" + group + "')");
								}
							}
						} catch (ObjectMappingException e) {
							this.plugin.getELogger().warn("Unable to read the list of commands : (group='" + group + "')");
						}
					}
					
				// Aucun groupe
				} else {
					Map<String, Long> cooldowns = new HashMap<String, Long>();
					// Liste des permissions
					for (Entry<Object, ? extends ConfigurationNode> cooldown : command.getValue().getChildrenMap().entrySet()) {
						if (cooldown.getKey() instanceof String) {
							String permission = (String) cooldown.getKey();
							if (!permission.equalsIgnoreCase(CooldownsService.NAME_DEFAULT)) {
								long value = cooldown.getValue().getLong(-1L);
								if (value >= 0) {
									cooldowns.put((String) cooldown.getKey(), value*1000);
								} else {
									this.plugin.getELogger().warn("The value of the cooldown is invalid : (name='" + group + "';permission='" + permission + "';value='" + value + "')");
								}
							}
						} else {
							this.plugin.getELogger().warn("The name of the permission is invalid : (name='" + group + "';permission='" + cooldown.getKey().toString() + "')");
						}
					}
					
					if (!commands.containsKey(group)) {
						commands.put(group, new EValue(command.getValue().getNode(CooldownsService.NAME_DEFAULT).getLong(0)*1000, UtilsMap.valueLinkedASC(cooldowns)));
					} else {
						this.plugin.getELogger().warn("The name is already used : (name='" + group + "')");
					}
				}
			} else {
				this.plugin.getELogger().warn("The group name is invalid : (group='" + command.getKey().toString() + "')");
			}
		}
		this.plugin.getELogger().info("Loading " + commands.size() + " cooldown(s)");
		return commands;
	}
}
