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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import fr.evercraft.everapi.exception.PluginDisableException;
import fr.evercraft.everapi.exception.ServerDisableException;
import fr.evercraft.everapi.plugin.EDataBase;

public class ECDataBase extends EDataBase<EverCooldowns> {
	private String table_player;

	public ECDataBase(EverCooldowns plugin) throws PluginDisableException {
		super(plugin, true);
	}

	public boolean init() throws ServerDisableException {
		this.table_player = "player";

		String player = "CREATE TABLE IF NOT EXISTS <table> (" + 
						"`uuid` varchar(36) NOT NULL," + 
						"`command` varchar(32) NOT NULL," + 
						"`time` timestamp NOT NULL," + 
						"PRIMARY KEY (`uuid`, `command`));";
	
		initTable(this.getTablePlayer(), player);
		return true;
	}

	public String getTablePlayer() {
		return this.getPrefix() + this.table_player;
	}
	
	public void remove() {
		Connection connection = null;
		PreparedStatement preparedStatement = null;
		try {
			connection = this.getConnection();
			String query = 	  "DELETE "
							+ "FROM " + this.plugin.getDataBases().getTablePlayer() +" " 
							+ "WHERE time < NOW();";
			preparedStatement = connection.prepareStatement(query);
			preparedStatement.execute();
			this.plugin.getLogger().debug("Clear database ");
		} catch (SQLException e) {
	    	this.plugin.getLogger().warn("Error during a change of account : " + e.getMessage());
		} catch (ServerDisableException e) {
			e.execute();
		} finally {
			try {
				if (preparedStatement != null) preparedStatement.close();
				if (connection != null) connection.close();
			} catch (SQLException e) {}
	    }
	}
}
