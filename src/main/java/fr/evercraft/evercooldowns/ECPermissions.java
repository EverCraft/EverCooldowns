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
package fr.evercraft.evercooldowns;

import fr.evercraft.everapi.plugin.EnumPermission;
import fr.evercraft.everapi.plugin.file.EnumMessage;
import fr.evercraft.evercooldowns.ECMessage.ECMessages;

public enum ECPermissions implements EnumPermission {
	EVERCOOLDOWNS("commands.execute", ECMessages.PERMISSIONS_COMMANDS_EXECUTE),
	HELP("commands.help", ECMessages.PERMISSIONS_COMMANDS_HELP),
	RELOAD("commands.reload", ECMessages.PERMISSIONS_COMMANDS_RELOAD),
	
	LIST("commands.list.execute", ECMessages.PERMISSIONS_COMMANDS_LIST_EXECUTE),
	LIST_OTHERS("commands.list.others", ECMessages.PERMISSIONS_COMMANDS_LIST_OTHERS),
	
	CLEAR("commands.clear.execute", ECMessages.PERMISSIONS_COMMANDS_CLEAR_EXECUTE),
	CLEAR_OTHERS("commands.clear.others", ECMessages.PERMISSIONS_COMMANDS_CLEAR_OTHERS),
	
	REMOVE("commands.remove.execute", ECMessages.PERMISSIONS_COMMANDS_REMOVE_EXECUTE),
	REMOVE_OTHERS("commands.remove.others", ECMessages.PERMISSIONS_COMMANDS_REMOVE_OTHERS),
	
	COOLDOWN("cooldown", ECMessages.PERMISSIONS_COOLDOWN),
	BYPASS("bypass", ECMessages.PERMISSIONS_BYPASS);

	private static final String PREFIX = "evercooldowns";
	
	private final String permission;
	private final EnumMessage message;
	private final boolean value;
    
	private ECPermissions(final String permission, final EnumMessage message) {
    	this(permission, message, false);
    }
    
    private ECPermissions(final String permission, final EnumMessage message, final boolean value) {   	    	
    	this.permission = PREFIX + "." + permission;
    	this.message = message;
    	this.value = value;
    }

    @Override
    public String get() {
    	return this.permission;
	}

	@Override
	public boolean getDefault() {
		return this.value;
	}

	@Override
	public EnumMessage getMessage() {
		return this.message;
	}
}
