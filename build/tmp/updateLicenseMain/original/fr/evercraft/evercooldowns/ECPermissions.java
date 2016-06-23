package fr.evercraft.evercooldowns;

import org.spongepowered.api.command.CommandSource;

import com.google.common.base.Preconditions;

import fr.evercraft.everapi.plugin.EnumPermission;

public enum ECPermissions implements EnumPermission {
	EVERCOOLDOWNS("command"),
	
	HELP("help"),
	RELOAD("reload");

	private final static String prefix = "evercooldowns";
	
	private final String permission;
    
    private ECPermissions(final String permission) {   	
    	Preconditions.checkNotNull(permission, "La permission '" + this.name() + "' n'est pas définit");
    	
    	this.permission = permission;
    }

    public String get() {
		return ECPermissions.prefix + "." + this.permission;
	}
    
    public boolean has(CommandSource player) {
    	return player.hasPermission(this.get());
    }
}
