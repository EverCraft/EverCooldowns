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

import java.util.Optional;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

import fr.evercraft.everapi.event.CommandEvent;
import fr.evercraft.evercooldowns.ECMessage.ECMessages;

public class ECListener {
	private EverCooldowns plugin;

	public ECListener(EverCooldowns plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * Ajoute le joueur dans le cache
	 */
	@Listener
    public void onClientConnectionEvent(final ClientConnectionEvent.Auth event) {
		this.plugin.getService().get(event.getProfile().getUniqueId());
    }
	
	/**
	 * Ajoute le joueur à la liste
	 */
	@Listener
    public void onClientConnectionEvent(final ClientConnectionEvent.Join event) {
		this.plugin.getService().registerPlayer(event.getTargetEntity().getUniqueId());
    }
    
	/**
	 * Supprime le joueur de la liste
	 */
    @Listener
    public void onClientConnectionEvent(final ClientConnectionEvent.Disconnect event) {
        this.plugin.getService().removePlayer(event.getTargetEntity().getUniqueId());
    }
    
    /**
     * Vérifie le cooldown
     */
    @Listener
    public void commandEvent(final CommandEvent.Send event) {
        if(!event.getPlayer().hasPermission(ECPermissions.BYPASS.get())) {
        	Optional<Long> cooldown = event.getPlayer().getCooldown(event.getCommand());
        	if(cooldown.isPresent()) {
        		event.setCancelled(true);
        		event.getPlayer().sendMessage(event.getPlayer().replaceVariable(ECMessages.PREFIX.get() + ECMessages.COOLDOWN.get()
        				.replaceAll("<time>", this.plugin.getEverAPI().getManagerUtils().getDate().formatDateDiff(cooldown.get()))));
        	}
        }
    }
    
    /**
     * Ajoute le cooldown
     */
    @Listener
    public void commandEvent(final CommandEvent.Result event) {
    	if(event.getResult() && !event.getPlayer().hasPermission(ECPermissions.BYPASS.get())) {
    		event.getPlayer().addCooldown(event.getCommand());
    	}
    }
}