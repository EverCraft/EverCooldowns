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

import com.google.common.base.Preconditions;

import fr.evercraft.everapi.message.EMessageBuilder;
import fr.evercraft.everapi.message.EMessageFormat;
import fr.evercraft.everapi.message.format.EFormatString;
import fr.evercraft.everapi.plugin.file.EMessage;
import fr.evercraft.everapi.plugin.file.EnumMessage;

public class ECMessage extends EMessage<EverCooldowns> {

	public ECMessage(final EverCooldowns plugin) {
		super(plugin, ECMessages.values());
	}
	
	public enum ECMessages implements EnumMessage {
		PREFIX("PREFIX", 									"[&4Ever&6&lCooldowns&f] "),
		DESCRIPTION("description", 							"Gestion des cooldowns"),
		COOLDOWN("cooldown", 								"&cVous ne pouvez pas effectuer cette action avant : {time}"),
		
		// Clear
		CLEAR_DESCRIPTION("clearDescription", 				"Supprime tous les cooldowns d'un joueur"),
		CLEAR_STAFF("clearStaff", 							"&7Tous les cooldowns de &6{player} &7ont bien été supprimés."),
		CLEAR_PLAYER("clearPlayer", 						"&7Tous vos cooldowns ont été supprimés par &6{staff}&7."),
		CLEAR_EQUALS("clearEquals", 						"&7Tous vos cooldowns ont bien été supprimés."),
		CLEAR_ALL("clearAll", 								"&7Tous les cooldowns ont bien été supprimés."),
		CLEAR_ERROR_STAFF("clearErrorStaff", 				"&6{player} &cn'a aucun cooldown."),
		CLEAR_ERROR_PLAYER("clearErrorEquals", 				"&cVous n'avez aucun cooldown."),
		
		// List
		LIST_DESCRIPTION("listDescription", 				"Affiche la liste des cooldowns d'un joueur"),
		LIST_PLAYER_TITLE("listPlayerTitle", 				"&aLa liste de vos cooldowns"),
		LIST_PLAYER_LINE("listPlayerLine", 					"    &6&l➤  &6{cooldown} : &a{time} {delete}"),
		LIST_PLAYER_EMPTY("listPlayerEmpty", 				"&7Vous n'avez aucun cooldown."),
		LIST_PLAYER_DELETE("listPlayerDelete", 				"&c&nSupprimer"),
		LIST_PLAYER_DELETE_HOVER("listPlayerDeleteHover", 	"&cCliquez ici pour supprimer le cooldown &6{cooldown}&c."),
		LIST_STAFF_TITLE("listStaffTitle", 					"&aLa liste des cooldowns de &6<player"),
		LIST_STAFF_LINE("listStaffLine", 					"    &6&l➤  &6{cooldown} : &a{time} {delete}"),
		LIST_STAFF_EMPTY("listStaffEmpty", 					"&6{player} &7n'a aucun cooldown."),
		LIST_STAFF_DELETE("listStaffDelete", 				"&c&nSupprimer"),
		LIST_STAFF_DELETE_HOVER("listStaffDeleteHover", 	"&cCliquez ici pour supprimer le cooldown &6{cooldown}&c."),
		
		// Remove
		REMOVE_DESCRIPTION("removeDescription", 			"Supprime un cooldown d'un joueur"),
		REMOVE_STAFF("removeStaff", 						"&7Le cooldown &6{cooldown} &7de &6{player} &7a bien été supprimé."),
		REMOVE_PLAYER("removePlayer",						"&7Votre cooldown &6{cooldown} &7a été supprimé par &6{staff}&7."),
		REMOVE_EQUALS("removeEquals", 						"&7Votre cooldown &6{cooldown} &7a bien été supprimé."),
		REMOVE_ERROR_STAFF("removeErrorStaff", 				"&6{player} &cn'a pas cooldown &6{cooldown}&7."),
		REMOVE_ERROR_PLAYER("removeErrorEquals", 			"&cVous n'avez pas cooldown &6{cooldown}&7.");
		
		private final String path;
	    private final EMessageBuilder french;
	    private final EMessageBuilder english;
	    private EMessageFormat message;
	    private EMessageBuilder builder;
	    
	    private ECMessages(final String path, final String french) {   	
	    	this(path, EMessageFormat.builder().chat(new EFormatString(french), true));
	    }
	    
	    private ECMessages(final String path, final String french, final String english) {   	
	    	this(path, 
	    		EMessageFormat.builder().chat(new EFormatString(french), true), 
	    		EMessageFormat.builder().chat(new EFormatString(english), true));
	    }
	    
	    private ECMessages(final String path, final EMessageBuilder french) {   	
	    	this(path, french, french);
	    }
	    
	    private ECMessages(final String path, final EMessageBuilder french, final EMessageBuilder english) {
	    	Preconditions.checkNotNull(french, "Le message '" + this.name() + "' n'est pas définit");
	    	
	    	this.path = path;	    	
	    	this.french = french;
	    	this.english = english;
	    	this.message = french.build();
	    }

	    public String getName() {
			return this.name();
		}
	    
		public String getPath() {
			return this.path;
		}

		public EMessageBuilder getFrench() {
			return this.french;
		}

		public EMessageBuilder getEnglish() {
			return this.english;
		}
		
		public EMessageFormat getMessage() {
			return this.message;
		}
		
		public EMessageBuilder getBuilder() {
			return this.builder;
		}
		
		public void set(EMessageBuilder message) {
			this.message = message.build();
			this.builder = message;
		}
	}
}
