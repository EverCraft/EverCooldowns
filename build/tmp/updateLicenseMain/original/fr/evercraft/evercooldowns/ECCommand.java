package fr.evercraft.evercooldowns;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;

import fr.evercraft.everapi.plugin.command.EParentCommand;
import fr.evercraft.evercooldowns.ECMessage.ECMessages;

public class ECCommand extends EParentCommand<EverCooldowns> {
	
	public ECCommand(final EverCooldowns plugin) {
        super(plugin, "evercooldowns");
    }
	
	@Override
	public boolean testPermission(final CommandSource source) {
		return source.hasPermission(ECPermissions.EVERCOOLDOWNS.get());
	}

	@Override
	public Text description(final CommandSource source) {
		return ECMessages.DESCRIPTION.getText();
	}

	@Override
	public boolean testPermissionHelp(final CommandSource source) {
		return source.hasPermission(ECPermissions.HELP.get());
	}
}