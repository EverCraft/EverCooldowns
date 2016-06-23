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
package fr.evercraft.evermails;

import org.spongepowered.api.command.CommandSource;

import com.google.common.base.Preconditions;

import fr.evercraft.everapi.plugin.EnumPermission;

public enum EMPermissions implements EnumPermission {
	EVERMAILS("command"),
	
	HELP("help"),
	RELOAD("reload"),
	
	LIST("manage.list"),
	SET("manage.set"),
	DELETE("manage.delete"),
	
	ALERT("use.alert"),
	SEND("use.send");
	
	private final static String prefix = "evermails";
	
	private final String permission;
    
    private EMPermissions(final String permission) {   	
    	Preconditions.checkNotNull(permission, "La permission '" + this.name() + "' n'est pas définit");
    	
    	this.permission = permission;
    }

    public String get() {
		return EMPermissions.prefix + "." + this.permission;
	}
    
    public boolean has(CommandSource player) {
    	return player.hasPermission(this.get());
    }
}
