package de.danielclasen;

/*
 This file is part of WoodCutterNPC

 Foobar is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Foobar is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
 */

import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.citizensnpcs.api.CitizensAPI;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class WoodCutterNPC extends JavaPlugin {

	// ClassListeners
	private final WoodCutterNPCCommandExecutor commandExecutor = new WoodCutterNPCCommandExecutor(
			this);
	private final WoodCutterNPCEventListener eventListener = new WoodCutterNPCEventListener(
			this);
	// ClassListeners

	private String currentArtifact = "WoodCutterNPC-0.0.1-SNAPSHOT.jar";
	
	public final Logger log = Bukkit.getServer().getLogger();

	public void onDisable() {
		// add any code you want to be executed when your plugin is disabled
	}

	public void onEnable() {

		PluginManager pm = this.getServer().getPluginManager();
		getCommand("command").setExecutor(commandExecutor);

		// you can register multiple classes to handle events if you want
		// just call pm.registerEvents() on an instance of each class
		pm.registerEvents(eventListener, this);

		if (Bukkit.getServer().getPluginManager().getPlugin("Citizens") == null
				|| Bukkit.getServer().getPluginManager().getPlugin("Citizens")
						.isEnabled() == false) {
			getLogger().log(Level.SEVERE,
					"Citizens 2.0 not found or not enabled");
			getServer().getPluginManager().disablePlugin(this);
			return;
		} else {
			// Register your trait with Citizens.
			CitizensAPI.getTraitFactory().registerTrait(
					net.citizensnpcs.api.trait.TraitInfo.create(
							WoodCutterNPCTrait.class).withName("Woodcutter"));

		}
		// do any other initialisation you need here...
	}

	private void checkUpdateAtJenkins() {
		JSONParser jsp = new JSONParser();
		URL jenkinsChannel;
		try {
			jenkinsChannel = new URL(
					"http://ci.danielclasen.de/jenkins/job/WoodCutterNPC/lastStableBuild/api/json?pretty=true");
			HttpURLConnection conn = (HttpURLConnection) jenkinsChannel
					.openConnection();
			conn.setRequestMethod("GET");
			conn.setUseCaches(false);
			if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
				String response = conn.getResponseMessage();
				System.out.println(response);
				log.info(response);
				JSONObject responseJSON = (JSONObject) jsp.parse(response);
				log.info("Latest Build Revision at Jenkins Repo is: "+responseJSON.get("number")+" ("+responseJSON.get("fullDisplayName")+")");
				
			} else {
				// Verarbeitung des Ergebnisses
			}
		} catch (Exception e) {// TODO: detailed exception handling, detailed
			// exceptions available, but ignored for the
			// moment
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// jsp.parse(arg0)
	}

	private void fetchUpdateFromJenkins(String targetArtifact) {
		URL jenkinsChannel;
		try {
			jenkinsChannel = new URL(
					"http://ci.danielclasen.de/jenkins/job/WoodCutterNPC/lastStableBuild/artifact/target/");
			ReadableByteChannel rbc = Channels.newChannel(jenkinsChannel
					.openStream());
			FileOutputStream fos = new FileOutputStream(targetArtifact);
			fos.getChannel().transferFrom(rbc, 0, 1 << 24);
		} catch (Exception e) { // TODO: detailed exception handling, detailed
								// exceptions available, but ignored for the
								// moment
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
