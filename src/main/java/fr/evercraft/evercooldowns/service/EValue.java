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

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.spongepowered.api.service.permission.Subject;

import fr.evercraft.evercooldowns.ECPermissions;

public class EValue {

	private final Long cooldown_default;
	
	private final LinkedHashMap<String, Long> cooldowns;
	
	public EValue(final Long cooldown_default, final LinkedHashMap<String, Long> cooldowns) {
		this.cooldown_default = cooldown_default;
		this.cooldowns = cooldowns;
	}

	public Long getCooldowns_default() {
		return cooldown_default;
	}

	public LinkedHashMap<String, Long> getCooldowns() {
		return cooldowns;
	}
	
	/**
	 * Donne la valeur du cooldown pour un Subject
	 * @param subject Le subject
	 * @return En Millisecondes
	 */
	public Long get(final Subject subject) {
		Long cooldown = null;
		
		Iterator<Map.Entry<String,Long>> cooldowns = this.cooldowns.entrySet().iterator();
		while(cooldowns.hasNext() && cooldown == null) {
			Entry<String, Long> iter = cooldowns.next();
			if (subject.hasPermission(ECPermissions.EVERCOOLDOWNS + "." + iter.getKey())) {
				cooldown = iter.getValue();
			}
		}
		
		if(cooldown != null) {
			return cooldown;
		}
		return this.cooldown_default;
	}
}
