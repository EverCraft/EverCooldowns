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

import fr.evercraft.everapi.exception.PluginDisableException;
import fr.evercraft.everapi.plugin.EPlugin;
import fr.evercraft.everapi.services.cooldown.CooldownsService;
import fr.evercraft.evercooldowns.command.sub.ECReload;
import fr.evercraft.evercooldowns.service.ECooldownsService;

@Plugin(id = "fr.evercraft.evercooldowns", 
		name = "EverCooldowns", 
		version = "1.2", 
		description = "Cooldowns",
		url = "http://evercraft.fr/",
		authors = {"rexbut"},
		dependencies = {
		    @Dependency(id = "fr.evercraft.everapi", version = "1.2")
		})
public class EverCooldowns extends EPlugin {
	private ECConfig configs;
	private ECMessage messages;
	private ECDataBase databases;
	
	private ECooldownsService service;
	
	@Override
	protected void onPreEnable() throws PluginDisableException {
		this.configs = new ECConfig(this);
		this.messages = new ECMessage(this);
		this.databases = new ECDataBase(this);
		
		this.service = new ECooldownsService(this);
		this.getGame().getServiceManager().setProvider(this, CooldownsService.class, this.service);
	}

	
	@Override
	protected void onCompleteEnable() {
		this.getGame().getEventManager().registerListeners(this, new ECListener(this));
		
		ECCommand command = new ECCommand(this);
		
		command.add(new ECReload(this, command));
	}

	protected void onReload() throws PluginDisableException{
		this.reloadConfigurations();
		
		this.databases.reload();
		
		this.service.reload();
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

	public ECDataBase getDataBases() {
		return this.databases;
	}
	
	public ECooldownsService getService() {
		return this.service;
	}
}
