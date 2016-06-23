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

import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import fr.evercraft.everapi.plugin.EPlugin;
import fr.evercraft.evercooldowns.command.sub.ECReload;

@Plugin(id = "fr.evercraft.evermails", 
		name = "EverMails", 
		version = "1.2", 
		description = "Sending mail",
		url = "http://evercraft.fr/",
		authors = {"rexbut"},
		dependencies = {
		    @Dependency(id = "fr.evercraft.everapi", version = "1.2"),
		    @Dependency(id = "fr.evercraft.everchat", optional = true)
		})
public class EverCooldowns extends EPlugin {
	private ECConfig configs;
	private ECMessage messages;
	
	//private ECooldownsService service;
	
	@Override
	protected void onPreEnable() {
		this.configs = new ECConfig(this);
		
		this.messages = new ECMessage(this);
		
		//this.service = new ECooldownsService(this);
		//this.getGame().getServiceManager().setProvider(this, MailService.class, this.service);
	}

	
	@Override
	protected void onCompleteEnable() {
		ECCommand command = new ECCommand(this);
		
		command.add(new ECReload(this, command));
	}

	protected void onReload(){
		this.reloadConfigurations();
		//this.service.reload();
	}
	
	protected void onDisable() {
	}

	/*
	 * Accesseurs
	 */
	
	public ECMessage getMessages(){
		return this.messages;
	}
	
	public ECConfig getConfigs() {
		return this.configs;
	}
	
	/*public EMailService getService() {
		return this.service;
	}*/
}
