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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.lang.annotation.Annotation;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.RegEx;
import javax.annotation.meta.When;

import net.citizensnpcs.api.CitizensAPI;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONArray;
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
	private boolean updatedCurrently = false;


	public Logger log;

	public void onDisable() {
		// add any code you want to be executed when your plugin is disabled
		checkUpdateAtJenkins();
	}

	public void onEnable() {
		
		log = getLogger();
		
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
				log.severe(response);

			} else {
				// Verarbeitung des Ergebnisses
				InputStream response = (InputStream) conn.getContent();

				BufferedReader reader = new BufferedReader(
						new InputStreamReader(response));
				String result, line = reader.readLine();
				result = line;
				while ((line = reader.readLine()) != null) {
					result += line;
				}
				JSONObject responseJSON = (JSONObject) jsp.parse(result);
				log.info("Latest Build Revision at Jenkins Repo is: "
						+ responseJSON.get("number") + " ("
						+ responseJSON.get("fullDisplayName") + ")");

				JSONArray artifacts = ((JSONArray) responseJSON
						.get("artifacts"));
				String targetArtifact = (String) ((JSONObject) artifacts.get(0))
						.get("fileName");

				fetchUpdateFromJenkins(targetArtifact);
			}
		} catch (Exception e) {// TODO: detailed exception handling, detailed
			// exceptions available, but ignored for the
			// moment
			// TODO Auto-generated catch block
			log.severe("there was an error!");
			e.printStackTrace();
		}

	}

	private void fetchUpdateFromJenkins(String targetArtifact) {
		URL jenkinsChannel;
		try {
			
			log.info("Fetching Update version: "+targetArtifact);
			jenkinsChannel = new URL(
					"http://ci.danielclasen.de/jenkins/job/WoodCutterNPC/lastStableBuild/artifact/target/"
							+ targetArtifact);
			ReadableByteChannel rbc = Channels.newChannel(jenkinsChannel
					.openStream());
			log.info("Fetching update complete, installing now.");
			try {
				deleteOldVersions();
				FileOutputStream fos = new FileOutputStream("plugins/"
						+ targetArtifact);
				fos.getChannel().transferFrom(rbc, 0, 1 << 24);
				log.info("Update successfully installed. Version: "+targetArtifact);
			} catch (IllegalArgumentException e) {
				// TODO: handle exception
				log.severe("Old version " + this.currentArtifact
						+ " could not be deleted! " + e.toString());
			}

		} catch (Exception e) { // TODO: detailed exception handling, detailed
								// exceptions available, but ignored for the
								// moment
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void deleteOldVersions() {

		File dir = new File("plugins");
		File[] files = dir.listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".jar")
						&& name.startsWith("WoodCutterNPC");
			}
		});

		for (File f : files) {
			
			log.info("Deleting old Version "+f.getName());
			// Make sure the file or directory exists and isn't write protected
			if (!f.exists())
				throw new IllegalArgumentException(
						"Delete: no such file or directory: " + f.getName());

			if (!f.canWrite())
				throw new IllegalArgumentException("Delete: write protected: "
						+ f.getName());

			// If it is a directory, make sure it is empty
			if (f.isDirectory()) {
				String[] files1 = f.list();
				if (files1.length > 0)
					throw new IllegalArgumentException(
							"Delete: directory not empty: " + f.getName());
			}

			// Attempt to delete it
			boolean success = f.delete();

			if (!success)
				throw new IllegalArgumentException("Delete: deletion failed");
		}
	}

}
