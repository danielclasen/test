package de.danielclasen;

import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;

import net.citizensnpcs.api.ai.speech.SpeechContext;
import net.citizensnpcs.api.persistence.Persist;
import net.citizensnpcs.api.trait.Trait;

public class WoodCutterNPCTrait extends Trait  {
	
	WoodCutterNPCTrait plugin = null;
	 
	protected boolean SomeSetting = false;
	
	public final Logger log = Bukkit.getServer().getLogger();
	
	public WoodCutterNPCTrait() {
		super("Woodcutter");
		plugin = (WoodCutterNPCTrait) Bukkit.getServer().getPluginManager().getPlugin("MyPlugin");
	}
	
	@Persist("mysettingname") boolean automaticallyPersistedSetting = false;
	
	// An example event handler. All traits will be registered automatically as Bukkit Listeners.
		@EventHandler
		public void click(net.citizensnpcs.api.event.NPCClickEvent event){
			//Handle a click on a NPC. The event has a getNPC() method. 
			//Be sure to check event.getNPC() == this.getNPC() so you only handle clicks on this NPC!
			
			if (event.getNPC() == this.getNPC()){
				event.getNPC().getDefaultSpeechController().speak(new SpeechContext("hello"));				
			}
	 
		}
	 
	        // Called every tick
	        @Override
	        public void run() {
	        }
	 
		//Run code when your trait is attached to a NPC. 
	        //This is called BEFORE onSpawn, so npc.getBukkitEntity() will return null
	        //This would be a good place to load configurable defaults for new NPCs.
		@Override
		public void onAttach() {
			log.info(npc.getName() + "has been assigned MyTrait!");
		}
	 
	        // Run code when the NPC is despawned. This is called before the entity actually despawns so npc.getBukkitEntity() is still valid.
		@Override
		public void onDespawn() {
	        }
	 
		//Run code when the NPC is spawned. Note that npc.getBukkitEntity() will be null until this method is called.
	        //This is called AFTER onAttach and AFTER Load when the server is started.
		@Override
		public void onSpawn() {
	 
		}
	 
	        //run code when the NPC is removed. Use this to tear down any repeating tasks.
		@Override
		public void onRemove() {
	        }
	 
}
