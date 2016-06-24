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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ninja.leaping.configurate.ConfigurationNode;
import fr.evercraft.everapi.java.UtilsMap;
import fr.evercraft.everapi.plugin.file.EConfig;
import fr.evercraft.everapi.services.cooldown.CooldownsService;
import fr.evercraft.evercooldowns.EverCooldowns;

public class ECooldownsConfig extends EConfig {

	public ECooldownsConfig(final EverCooldowns plugin) {
		super(plugin, "cooldowns");
	}
	
	@Override
	public void loadDefault() {
		if(this.getNode().getValue() == null) {
			HashMap<List<String>, HashMap<String, Integer>> list = new HashMap<List<String>, HashMap<String, Integer>>();
			HashMap<String, Integer> number = new HashMap<String, Integer>();
			number.put("moderateur", 5);
			number.put("default", 10);
			list.put(Arrays.asList("ping", "heal", "food"), number);
			this.getNode().setValue(list);
		}
		addDefault("default.default", 1);
	}
	
	public Map<String, ECooldownsValue> getCooldowns() {
		Map<String, ECooldownsValue> commands = new HashMap<String, ECooldownsValue>();
		
		for (Entry<Object, ? extends ConfigurationNode> command : this.getNode().getChildrenMap().entrySet()) {
			if(command.getKey() instanceof String || command.getKey() instanceof List) {
				Map<String, Long> cooldowns = new HashMap<String, Long>();
				for (Entry<Object, ? extends ConfigurationNode> cooldown : command.getValue().getChildrenMap().entrySet()) {
					if(cooldown.getKey() instanceof String && !((String) cooldown.getKey()).equalsIgnoreCase(CooldownsService.NAME_DEFAULT)) {
						long value = cooldown.getValue().getLong(-1L);
						if(value >= 0) {
							cooldowns.put((String) cooldown.getKey(), value*1000);
						}
					}
				}
				
				ECooldownsValue value = new ECooldownsValue(command.getValue().getNode(CooldownsService.NAME_DEFAULT).getLong(0)*1000, UtilsMap.valueLinkedASC(cooldowns));
				if(command.getKey() instanceof String) {
					commands.put((String) command.getKey(), value);
				} else {
					for(Object name : ((List<?>) command.getKey())) {
						if(name instanceof String) {
							commands.put((String) name, value);
						}
					}
				}
			}
		}
		return commands;
	}
}
