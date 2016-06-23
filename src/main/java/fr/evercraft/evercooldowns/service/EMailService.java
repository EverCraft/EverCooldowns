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
package fr.evercraft.evermails.service;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.MimeMessage;

import com.google.common.base.Preconditions;

import fr.evercraft.everapi.services.MailService;
import fr.evercraft.evermails.EverMails;

public class EMailService implements MailService {
	
	private final EverMails plugin;
	
	private boolean logger;
	
	private ConcurrentMap<String, String> mails;
	
	public EMailService(final EverMails plugin) {
		this.plugin = plugin;
		
		reload();
	}
	
	public void reload() {
		this.logger = this.plugin.getConfigs().getLogger();
		
		this.mails = new ConcurrentHashMap<String, String>(this.plugin.getConfigs().getMails());
	}
	
	@Override
	public boolean send(final String destinataire, final String object, final String message) {
		Preconditions.checkNotNull(destinataire, "destinataire");
		Preconditions.checkNotNull(object, "object");
		Preconditions.checkNotNull(message, "message");
		
		boolean resultat = false;
		
		Properties properties = new Properties(); 		
		Session session;
		if(this.plugin.getConfigs().getAuthentification()) {
			properties.put("mail.smtp.auth", String.valueOf(this.plugin.getConfigs().getAuthentification()));
			properties.put("mail.smtp.starttls.enable", String.valueOf(this.plugin.getConfigs().getStarttls()));
			properties.put("mail.smtp.host", String.valueOf(this.plugin.getConfigs().getHost()));
			properties.put("mail.smtp.port", String.valueOf(this.plugin.getConfigs().getPort()));
			
			session = Session.getInstance(properties,
			      new javax.mail.Authenticator() {
			         protected PasswordAuthentication getPasswordAuthentication() {
			            return new PasswordAuthentication(EMailService.this.plugin.getConfigs().getUserName(), EMailService.this.plugin.getConfigs().getPassword());
			         }
			      });
		} else {
			properties.put("mail.smtp.host", String.valueOf(this.plugin.getConfigs().getHost()));
			session = Session.getDefaultInstance(properties);
		}
	    
	    MimeMessage minemessage = new MimeMessage(session);
	    Transport transport = null;
	    try { 
	    	minemessage.setText(message); 
	        minemessage.setSubject(object); 
	        minemessage.addRecipients(Message.RecipientType.TO, destinataire);
	        
	        Transport.send(minemessage);
	        this.log(destinataire, object, message);
	        resultat = true;
	    } catch (MessagingException e) { 
	        this.plugin.getLogger().warn("Error when sending an email (Address='" + destinataire + "';Object='" + object + "';Message='" + message + "') : " + e.getMessage());
	    } finally { 
	        try { if (transport != null) transport.close(); } catch (MessagingException e) {} 
	    } 
	    return resultat;
	}
	
	@Override
	public boolean alert(final String object, final String message) {
		Preconditions.checkNotNull(object, "object");
		Preconditions.checkNotNull(message, "message");
		
		boolean resultat = false;
		for(String adress : this.mails.values()) {
			resultat = send(adress, object, message) || resultat;
		}
		return resultat;
	}
	
	public void log(final String destinataire, final String object, final String message) {
		if(this.logger) {
			this.plugin.getLogger().info("Mail sent to '" + destinataire + "' : (Object='" + object + "';Message='" + message + "')");
		}
	}

	public Map<String, String> getMails() {
		return this.mails;
	}

	public boolean setMail(String identifier, String address) {
		Preconditions.checkNotNull(identifier, "identifier");
		Preconditions.checkNotNull(address, "address");
		
		Pattern pattern = Pattern.compile(MailService.EMAIL_PATTERN);
		if(pattern.matcher(address).matches()) {
			this.plugin.getConfigs().setMail(identifier, address);
			this.mails.put(identifier, address);
			return true;
		}
		return false;
	}

	public void removeMail(String identifier) {
		Preconditions.checkNotNull(identifier, "identifier");
		
		this.plugin.getConfigs().removeMail(identifier);
		this.mails.remove(identifier);
	}
}
