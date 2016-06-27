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

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Preconditions;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import fr.evercraft.everapi.java.Chronometer;
import fr.evercraft.everapi.services.cooldown.CooldownsService;
import fr.evercraft.everapi.services.cooldown.CooldownsSubject;
import fr.evercraft.evercooldowns.EverCooldowns;

public class ECooldownsService implements CooldownsService {
	private final EverCooldowns plugin;
	
	private final ConcurrentMap<UUID, ESubject> subjects;
	private final LoadingCache<UUID, ESubject> cache;
	
	private final ConcurrentMap<String, EValue> commands;
	private EValue command_default;
	private final ECooldownsConfig config;
	

	public ECooldownsService(final EverCooldowns plugin) {		
		this.plugin = plugin;
		
		this.config = new ECooldownsConfig(this.plugin);
		
		this.commands = new ConcurrentHashMap<String, EValue>();
		this.subjects = new ConcurrentHashMap<UUID, ESubject>();
		this.cache = CacheBuilder.newBuilder()
					    .maximumSize(100)
					    .expireAfterAccess(5, TimeUnit.MINUTES)
					    .build(new CacheLoader<UUID, ESubject>() {
					    	/**
					    	 * Ajoute un joueur au cache
					    	 */
					        @Override
					        public ESubject load(UUID uuid){
					        	Chronometer chronometer = new Chronometer();
					        	
					        	ESubject subject = new ESubject(ECooldownsService.this.plugin, uuid);
					        	ECooldownsService.this.plugin.getLogger().debug("Loading user '" + uuid.toString() + "' in " +  chronometer.getMilliseconds().toString() + " ms");
					            return subject;
					        }
					    });
		this.load();
	}
	
	/**
	 * Rechargement : Vide le cache et recharge tous les joueurs
	 */
	public void reload() {
		this.plugin.getDataBases().remove();
		
		this.config.reload();
		
		this.commands.clear();
		
		this.load();
		
		this.cache.cleanUp();
		for(ESubject subject : this.subjects.values()) {
			subject.reload();
		}
	}
	
	public void load() {
		this.commands.putAll(this.config.getCooldowns());
		
		this.command_default = this.commands.get(CooldownsService.NAME_DEFAULT);
		if(this.command_default == null) {
			this.command_default = new EValue(CooldownsService.DEFAULT, new LinkedHashMap<String, Long>());
		}
	}
	
	public void clearAll() {
		this.plugin.getThreadAsync().execute(() -> this.clearAllAsync());
	}
	
	public void clearAllAsync() {
		this.plugin.getDataBases().clearAll();
		
		this.cache.cleanUp();
		for(ESubject subject : this.subjects.values()) {
			subject.reload();
		}
	}
	
	public EValue getCommands(String command) {
		EValue cooldown = this.commands.get(command);
		if(cooldown != null) {
			return cooldown;
		}
		return this.command_default;
	}
	
	
	@Override
	public Optional<CooldownsSubject> get(UUID uuid) {
		return Optional.ofNullable(this.getSubject(uuid).orElse(null));
	}
	
	public Optional<ESubject> getSubject(UUID uuid) {
		Preconditions.checkNotNull(uuid, "uuid");
		
		try {
			if(!this.subjects.containsKey(uuid)) {
				return Optional.ofNullable(this.cache.get(uuid));
	    	}
	    	return Optional.ofNullable(this.subjects.get(uuid));
		} catch (ExecutionException e) {
			this.plugin.getLogger().warn("Error : Loading user (identifier='" + uuid + "';message='" + e.getMessage() + "')");
			return Optional.empty();
		}
	}
	
	@Override
	public boolean hasRegistered(UUID uuid) {
		Preconditions.checkNotNull(uuid, "uuid");
		
		try {
			return this.plugin.getGame().getServer().getPlayer(uuid).isPresent();
		} catch (IllegalArgumentException e) {}
		return false;
	}
	
	/**
	 * Ajoute un joueur à la liste
	 * @param identifier L'UUID du joueur
	 */
	public void registerPlayer(UUID uuid) {
		Preconditions.checkNotNull(uuid, "uuid");
		
		ESubject player = this.cache.getIfPresent(uuid);
		// Si le joueur est dans le cache
		if(player != null) {
			this.subjects.putIfAbsent(uuid, player);
			this.plugin.getLogger().debug("Loading player cache : " + uuid.toString());
		// Si le joueur n'est pas dans le cache
		} else {
			Chronometer chronometer = new Chronometer();
			player = new ESubject(this.plugin, uuid);
			this.subjects.putIfAbsent(uuid, player);
			this.plugin.getLogger().debug("Loading player '" + uuid.toString() + "' in " +  chronometer.getMilliseconds().toString() + " ms");
		}
		//this.plugin.getManagerEvent().post(player, PermUserEvent.Action.USER_ADDED);
	}
	
	/**
	 * Supprime un joueur à la liste et l'ajoute au cache
	 * @param identifier L'UUID du joueur
	 */
	public void removePlayer(UUID uuid) {
		Preconditions.checkNotNull(uuid, "uuid");
		
		ESubject player = this.subjects.remove(uuid);
		// Si le joueur existe
		if(player != null) {
			this.cache.put(uuid, player);
			//this.plugin.getManagerEvent().post(player, PermUserEvent.Action.USER_REMOVED);
			this.plugin.getLogger().debug("Unloading the player : " + uuid.toString());
		}
	}

	@Override
	public Collection<CooldownsSubject> getAll() {
		Set<CooldownsSubject> list = new HashSet<CooldownsSubject>();
		list.addAll(this.subjects.values());
		list.addAll(this.cache.asMap().values());
		return list;
	}
}