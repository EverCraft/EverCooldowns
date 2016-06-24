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

import fr.evercraft.everapi.plugin.file.EConfig;
import fr.evercraft.evercooldowns.EverCooldowns;

public class ECooldownsConfig extends EConfig {

	public ECooldownsConfig(final EverCooldowns plugin) {
		super(plugin, "cooldowns");
	}
	
	@Override
	public void loadDefault() {
		if(this.getNode().getValue() == null) {
			addDefault("ping.admin", 1);
			addDefault("ping.moderateur", 2);
			addDefault("ping.default", 5);
		}
		addDefault("default.default", 1);
	}
}
