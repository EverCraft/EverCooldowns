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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.spongepowered.api.service.permission.Subject;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;

import fr.evercraft.everapi.exception.ServerDisableException;
import fr.evercraft.everapi.server.player.EPlayer;
import fr.evercraft.everapi.services.cooldown.CooldownsSubject;
import fr.evercraft.evercooldowns.EverCooldowns;

public class ESubject implements CooldownsSubject {
	
	private final EverCooldowns plugin;
	
	private final UUID identifier;

	private final ConcurrentHashMap<String, Long> cooldowns;
	
	public ESubject(final EverCooldowns plugin, final UUID uuid) {
		Preconditions.checkNotNull(plugin, "plugin");
		Preconditions.checkNotNull(uuid, "uuid");
		
		this.plugin = plugin;
		this.identifier = uuid;
		this.cooldowns = new ConcurrentHashMap<String, Long>();
		
		this.load();
	}
	
	public void reload() {
		this.cooldowns.clear();
		this.loadCooldowns();
	}

	public void load() {
		Connection connection = null;
		try {
			connection = this.plugin.getDataBases().getConnection();
			this.deleteCooldowns(connection);
			this.loadCooldowns(connection);
		} catch (ServerDisableException e) {
			e.execute();
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {}	
		}
	}
	
	/**
	 * Supprime les anciens cooldowns
	 * @param connection
	 */
	public void deleteCooldowns(final Connection connection) {
		PreparedStatement preparedStatement = null;
		String query = 	  "DELETE "
						+ "FROM " + this.plugin.getDataBases().getTablePlayer() +" " 
						+ "WHERE uuid = ? "
						+ "AND time < NOW();";
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, this.identifier.toString());
			preparedStatement.execute();
		} catch (SQLException e) {
			this.plugin.getLogger().warn("Error while loading data (uuid='" + this.identifier + "') : " + e.getMessage());
		} finally {
			try {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
			} catch (SQLException e) {}	
		}
	}
	
	/**
	 * Charge les cooldowns
	 */
	public void loadCooldowns() {
		Connection connection = null;
		try {
			connection = this.plugin.getDataBases().getConnection();
			this.loadCooldowns(connection);
		} catch (ServerDisableException e) {
			e.execute();
		} finally {
			try {
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException e) {}	
		}
	}
	
	/**
	 * Charge les cooldowns
	 * @param connection
	 */
	public void loadCooldowns(final Connection connection) {
		PreparedStatement preparedStatement = null;
		String query = 	  "SELECT command, time "
						+ "FROM " + this.plugin.getDataBases().getTablePlayer() +" " 
						+ "WHERE uuid = ? ;";
		try {
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, this.identifier.toString());
			ResultSet result = preparedStatement.executeQuery();
			while(result.next()) {
				this.cooldowns.put(result.getString("command"), result.getTimestamp("time").getTime());
			}
		} catch (SQLException e) {
			this.plugin.getLogger().warn("Error while loading data (uuid='" + this.identifier + "') : " + e.getMessage());
		} finally {
			try {
				if (preparedStatement != null) {
					preparedStatement.close();
				}
			} catch (SQLException e) {}	
		}
	}

	public void update() {
		List<String> removes = new ArrayList<String>();
		long time = System.currentTimeMillis();
		for (Entry<String, Long> cooldown : this.cooldowns.entrySet()) {
			if (cooldown.getValue() < time) {
				removes.add(cooldown.getKey());
			}
		}
		
		for (String remove : removes) {
			this.remove(remove);
		}
	}
	
	@Override
	public Map<String, Long> getAll() {
		this.update();
		return ImmutableMap.copyOf(this.cooldowns);
	}
	
	@Override
	public boolean add(final String command) {
		Optional<EPlayer> player = this.plugin.getEServer().getEPlayer(this.identifier);
		if (player.isPresent()) {
			return this.add(player.get().get(), command);
		}
		return false;
	}

	@Override
	public boolean add(final Subject subject, final String command) {
		Optional<Long> cooldown = this.getCooldown(subject, command);
		if (cooldown.isPresent()) {
			long time = System.currentTimeMillis() + cooldown.get();
			
			if (this.cooldowns.get(command) == null) {
				this.addDatabase(command, time);
			} else {
				this.updateDatabase(command, time);
			}
			
			this.cooldowns.put(command, time);
			return true;
		}
		return false;
	}

	@Override
	public boolean remove(final String command) {
		if (this.cooldowns.remove(command) != null) {
			this.removeDatabase(command);
			return true;
		}
		return false;
	}
	
	@Override
	public boolean clear() {
		if (!this.cooldowns.isEmpty()) {
			this.cooldowns.clear();
			this.clearDatabase();
			return true;
		}
		return false;
	}

	@Override
	public Optional<Long> get(final String command) {
		Long time = this.cooldowns.get(command);
		if (time != null) {
			if (time < System.currentTimeMillis()) {
				this.cooldowns.remove(command);
				this.removeDatabase(command);
			} else {
				return Optional.of(time);
			}
		}
		return Optional.empty();
	}
	
	private Optional<Long> getCooldown(final Subject player, final String command) {
		Long cooldown = this.plugin.getService().getCommands(command).get(player);
		if (cooldown > 0) {
			return Optional.of(cooldown);
		}
		return Optional.empty();
	}
	
	/*
	 * DataBase
	 */
	
	private void addDatabase(final String command, final long time) {
		this.plugin.getThreadAsync().execute(() -> this.addDatabaseAsync(command, time));
	}
	
	private void updateDatabase(final String command, final long time) {
		this.plugin.getThreadAsync().execute(() -> this.updateDatabaseAsync(command, time));
	}
	
	private void removeDatabase(final String command) {
		this.plugin.getThreadAsync().execute(() -> this.removeDatabaseAsync(command));
	}
	
	private void clearDatabase() {
		this.plugin.getThreadAsync().execute(() -> this.clearDatabaseAsync());
	}
	
	private void addDatabaseAsync(final String command, final long time) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			connection = this.plugin.getDataBases().getConnection();
			String query = "INSERT INTO `" + this.plugin.getDataBases().getTablePlayer() + "` VALUES(?, ?, ?);";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, this.identifier.toString());
			preparedStatement.setString(2, command);
			preparedStatement.setTimestamp(3, new Timestamp(time));
			preparedStatement.execute();
			this.plugin.getLogger().debug("Adding to the database : (identifier='" + this.identifier + "';"
																	+ "command='" + command + "';"
																	+ "time='" + time + "')");
		} catch (SQLException e) {
	    	this.plugin.getLogger().warn("Error during a change of account : (identifier:'" + this.identifier + "'): " + e.getMessage());
		} catch (ServerDisableException e) {
			e.execute();
		} finally {
			try {
				if (preparedStatement != null) preparedStatement.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {}
	    }
	}
	
	private void updateDatabaseAsync(final String command, final long time) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			connection = this.plugin.getDataBases().getConnection();
			String query =    "UPDATE `" + this.plugin.getDataBases().getTablePlayer() + "` "
							+ "SET `time` = ? "
							+ "WHERE `uuid` = ? "
							+ "AND `command` = ?;";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setTimestamp(1, new Timestamp(time));
			preparedStatement.setString(2, this.identifier.toString());
			preparedStatement.setString(3, command);
			preparedStatement.execute();
			this.plugin.getLogger().debug("Update database : (identifier='" + this.identifier + "';"
																	+ "command='" + command + "';"
																	+ "time='" + time + "')");
		} catch (SQLException e) {
	    	this.plugin.getLogger().warn("Error during a change of account : (identifier:'" + this.identifier + "'): " + e.getMessage());
		} catch (ServerDisableException e) {
			e.execute();
		} finally {
			try {
				if (preparedStatement != null) preparedStatement.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {}
	    }
	}

	public void removeDatabaseAsync(final String command) {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			connection = this.plugin.getDataBases().getConnection();
			String query = 	  "DELETE "
							+ "FROM " + this.plugin.getDataBases().getTablePlayer() +" " 
							+ "WHERE uuid = ? "
							+ "AND command = ? ;";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, this.identifier.toString());
			preparedStatement.setString(2, command);
			preparedStatement.execute();
			this.plugin.getLogger().debug("Remove database : (identifier='" + this.identifier + "';"
																	+ "command='" + command + "')");
		} catch (SQLException e) {
	    	this.plugin.getLogger().warn("Error during a change of account : (identifier:'" + this.identifier + "'): " + e.getMessage());
		} catch (ServerDisableException e) {
			e.execute();
		} finally {
			try {
				if (preparedStatement != null) preparedStatement.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {}
	    }
	}
	
	public void clearDatabaseAsync() {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			connection = this.plugin.getDataBases().getConnection();
			String query = 	  "DELETE "
							+ "FROM " + this.plugin.getDataBases().getTablePlayer() +" " 
							+ "WHERE uuid = ? ;";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.setString(1, this.identifier.toString());
			preparedStatement.execute();
			this.plugin.getLogger().debug("Remove database : (identifier='" + this.identifier + "')");
		} catch (SQLException e) {
	    	this.plugin.getLogger().warn("Error during a change of account : (identifier:'" + this.identifier + "'): " + e.getMessage());
		} catch (ServerDisableException e) {
			e.execute();
		} finally {
			try {
				if (preparedStatement != null) preparedStatement.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {}
	    }
	}

	/*
	 * Accesseur
	 */
	
	public UUID getUniqueId() {
		return this.identifier;
	}
}