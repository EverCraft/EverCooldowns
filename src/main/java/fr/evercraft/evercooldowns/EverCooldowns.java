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

import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;

import fr.evercraft.everapi.plugin.EPlugin;
import fr.evercraft.everapi.services.MailService;
import fr.evercraft.evermails.command.sub.EMAlert;
import fr.evercraft.evermails.command.sub.EMDelete;
import fr.evercraft.evermails.command.sub.EMList;
import fr.evercraft.evermails.command.sub.EMReload;
import fr.evercraft.evermails.command.sub.EMSend;
import fr.evercraft.evermails.command.sub.EMSet;
import fr.evercraft.evermails.service.EMailService;

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
public class EverMails extends EPlugin {
	private EMConfig configs;
	private EMMessage messages;
	
	private EMailService service;
	
	@Override
	protected void onPreEnable() {
		this.configs = new EMConfig(this);
		
		this.messages = new EMMessage(this);
		
		this.service = new EMailService(this);
		this.getGame().getServiceManager().setProvider(this, MailService.class, this.service);
	}

	
	@Override
	protected void onCompleteEnable() {
		EMCommand command = new EMCommand(this);
		
		command.add(new EMReload(this, command));
		command.add(new EMAlert(this, command));
		command.add(new EMDelete(this, command));
		command.add(new EMList(this, command));
		command.add(new EMSend(this, command));
		command.add(new EMSet(this, command));
	}

	protected void onReload(){
		this.reloadConfigurations();
		this.service.reload();
	}
	
	protected void onDisable() {
	}

	/*
	 * Accesseurs
	 */
	
	public EMMessage getMessages(){
		return this.messages;
	}
	
	public EMConfig getConfigs() {
		return this.configs;
	}
	
	public EMailService getService() {
		return this.service;
	}
}
