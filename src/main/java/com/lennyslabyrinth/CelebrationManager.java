package com.lennyslabyrinth;

import net.runelite.api.Client;
import net.runelite.api.SoundEffectID;
import net.runelite.api.coords.WorldPoint;
import net.runelite.client.callback.ClientThread;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;

@Slf4j
@Singleton
public class CelebrationManager
{
	@Inject
	private Client client;

	@Inject
	private ClientThread clientThread;

	@Inject
	private LennysLabyrinthConfig config;

	public void triggerVictoryCelebration(String puzzleName)
	{
		clientThread.invokeLater(() -> {
			log.info("Triggering victory celebration for puzzle: {}", puzzleName);

			if (config.showCelebrationSound())
			{
				playVictorySound();
			}

			if (config.showCelebrationFireworks())
			{
				triggerFireworks();
			}
		});
	}

	private void playVictorySound()
	{
		try
		{
			// Play a celebratory sound - using NPC teleport woosh which has a magical/triumphant feel
			client.playSoundEffect(SoundEffectID.NPC_TELEPORT_WOOSH);
			log.debug("Played victory sound effect");
		}
		catch (Exception e)
		{
			log.warn("Failed to play victory sound", e);
		}
	}

	private void triggerFireworks()
	{
		try
		{
			if (client.getLocalPlayer() != null)
			{
				// For now, we'll play an additional celebratory sound instead of visual fireworks
				// Visual effects like SpotAnim require more complex implementation with game ticks
				// This could be enhanced later with proper SpotAnim usage
				client.playSoundEffect(SoundEffectID.GE_INCREMENT_PLOP);
				log.debug("Triggered fireworks sound effect");
			}
		}
		catch (Exception e)
		{
			log.warn("Failed to trigger fireworks", e);
		}
	}
}