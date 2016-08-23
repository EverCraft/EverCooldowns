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

import java.util.Arrays;
import java.util.List;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;

import com.google.common.base.Preconditions;

import fr.evercraft.everapi.plugin.EChat;
import fr.evercraft.everapi.plugin.file.EMessage;
import fr.evercraft.everapi.plugin.file.EnumMessage;

public class ECMessage extends EMessage {

	public ECMessage(final EverCooldowns plugin) {
		super(plugin, ECMessages.values());
	}
	
	public enum ECMessages implements EnumMessage {
		PREFIX("prefix", "[&4Ever&6&lCooldowns&f] "),
		DESCRIPTION("description", "Gestion des cooldowns"),
		COOLDOWN("cooldown", "&cVous ne pouvez pas effectuer cette action avant : <time>"),
		
		// Clear
		CLEAR_DESCRIPTION("clear.description", "Supprime tous les cooldowns d'un joueur"),
		CLEAR_STAFF("clear.staff", "&7Tous les cooldowns de &6<player> &7ont bien été supprimés."),
		CLEAR_PLAYER("clear.player", "&7Tous vos cooldowns ont été supprimés par &6<staff>&7."),
		CLEAR_EQUALS("clear.equals", "&7Tous vos cooldowns ont bien été supprimés."),
		CLEAR_ALL("clear.all", "&7Tous les cooldowns ont bien été supprimés."),
		CLEAR_ERROR_STAFF("clear.errorStaff", "&6<player> &cn'a aucun cooldown."),
		CLEAR_ERROR_PLAYER("clear.errorEquals", "&cVous n'avez aucun cooldown."),
		
		// List
		LIST_DESCRIPTION("list.description", "Affiche la liste des cooldowns d'un joueur"),
		LIST_PLAYER_TITLE("list.playerTitle", "&aLa liste de vos cooldowns"),
		LIST_PLAYER_LINE("list.playerLine", "    &6&l➤  &6<cooldown> : &a<time> <delete>"),
		LIST_PLAYER_EMPTY("list.playerEmpty", "&7Vous n'avez aucun cooldown."),
		LIST_PLAYER_DELETE("list.playerDelete", "&c&nSupprimer"),
		LIST_PLAYER_DELETE_HOVER("list.playerDeleteHover", "&cCliquez ici pour supprimer le cooldown &6<cooldown>&c."),
		LIST_STAFF_TITLE("list.staffTitle", "&aLa liste des cooldowns de &6<player"),
		LIST_STAFF_LINE("list.staffLine", "    &6&l➤  &6<cooldown> : &a<time> <delete>"),
		LIST_STAFF_EMPTY("list.staffEmpty", "&6<player> &7n'a aucun cooldown."),
		LIST_STAFF_DELETE("list.staffDelete", "&c&nSupprimer"),
		LIST_STAFF_DELETE_HOVER("list.staffDeleteHover", "&cCliquez ici pour supprimer le cooldown &6<cooldown>&c."),
		
		// Remove
		REMOVE_DESCRIPTION("remove.description", "Supprime un cooldown d'un joueur"),
		REMOVE_STAFF("remove.staff", "&7Le cooldown &6<cooldown> &7de &6<player> &7a bien été supprimé."),
		REMOVE_PLAYER("remove.player", "&7Votre cooldown &6<cooldown> &7a été supprimé par &6<staff>&7."),
		REMOVE_EQUALS("remove.equals", "&7Votre cooldown &6<cooldown> &7a bien été supprimé."),
		REMOVE_ERROR_STAFF("remove.errorStaff", "&6<player> &cn'a pas cooldown &6<cooldown>&7."),
		REMOVE_ERROR_PLAYER("remove.errorEquals", "&cVous n'avez pas cooldown &6<cooldown>&7.");
		
		private final String path;
	    private final Object french;
	    private final Object english;
	    private Object message;
	    
	    private ECMessages(final String path, final Object french) {   	
	    	this(path, french, french);
	    }
	    
	    private ECMessages(final String path, final Object french, final Object english) {
	    	Preconditions.checkNotNull(french, "Le message '" + this.name() + "' n'est pas définit");
	    	
	    	this.path = path;	    	
	    	this.french = french;
	    	this.english = english;
	    	this.message = french;
	    }

	    public String getName() {
			return this.name();
		}
	    
		public String getPath() {
			return this.path;
		}

		public Object getFrench() {
			return this.french;
		}

		public Object getEnglish() {
			return this.english;
		}
		
		public String get() {
			if (this.message instanceof String) {
				return (String) this.message;
			}
			return this.message.toString();
		}
			
		@SuppressWarnings("unchecked")
		public List<String> getList() {
			if (this.message instanceof List) {
				return (List<String>) this.message;
			}
			return Arrays.asList(this.message.toString());
		}
		
		public void set(Object message) {
			this.message = message;
		}

		public Text getText() {
			return EChat.of(this.get());
		}
		
		public TextColor getColor() {
			return EChat.getTextColor(this.get());
		}
	}
}
