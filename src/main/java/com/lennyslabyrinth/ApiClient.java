package com.lennyslabyrinth;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Singleton
public class ApiClient
{
	private static final String API_BASE_URL = "http://localhost:8080";
	private static final String SUBMIT_GUESS_ENDPOINT = "/submit-guess";
	private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

	private final OkHttpClient httpClient;
	private final Gson gson;

	@Inject
	public ApiClient()
	{
		this.httpClient = new OkHttpClient();
		this.gson = new Gson();
	}

	public CompletableFuture<ApiResponse> submitGuess(GameStateSubmission gameState)
	{
		CompletableFuture<ApiResponse> future = new CompletableFuture<>();

		try
		{
			String json = gson.toJson(gameState);
			log.debug("Submitting game state JSON: {}", json);

			RequestBody body = RequestBody.create(JSON, json);
			Request request = new Request.Builder()
				.url(API_BASE_URL + SUBMIT_GUESS_ENDPOINT)
				.post(body)
				.build();

			httpClient.newCall(request).enqueue(new Callback()
			{
				@Override
				public void onFailure(Call call, IOException e)
				{
					log.error("API request failed", e);
					future.complete(ApiResponse.error("Network error: " + e.getMessage()));
				}

				@Override
				public void onResponse(Call call, Response response)
				{
					try (response)
					{
						String responseBody = response.body() != null ? response.body().string() : "";
						log.debug("API response code: {}, body: {}", response.code(), responseBody);

						if (response.isSuccessful())
						{
							try
							{
								ApiResponse apiResponse = gson.fromJson(responseBody, ApiResponse.class);
								future.complete(apiResponse);
							}
							catch (JsonSyntaxException e)
							{
								log.error("Failed to parse API response JSON", e);
								future.complete(ApiResponse.error("Invalid response format"));
							}
						}
						else
						{
							String errorMessage = String.format("Server error %d: %s", response.code(), responseBody);
							future.complete(ApiResponse.error(errorMessage));
						}
					}
					catch (IOException e)
					{
						log.error("Failed to read response body", e);
						future.complete(ApiResponse.error("Failed to read server response"));
					}
				}
			});
		}
		catch (Exception e)
		{
			log.error("Failed to create API request", e);
			future.complete(ApiResponse.error("Request creation failed: " + e.getMessage()));
		}

		return future;
	}

	// Inner classes for data structures matching the server API
	public static class GameStateSubmission
	{
		public LocationData location;
		public List<ItemData> inventory;
		public List<ItemData> worn_items;
		public Integer emote_id;
		public Integer npc_id;
		public String interaction_type;
		public String event_key;
		public String rsn;

		public GameStateSubmission(LocationData location, List<ItemData> inventory, 
			List<ItemData> wornItems, Integer emoteId, Integer npcId, 
			String interactionType, String eventKey, String rsn)
		{
			this.location = location;
			this.inventory = inventory;
			this.worn_items = wornItems;
			this.emote_id = emoteId;
			this.npc_id = npcId;
			this.interaction_type = interactionType;
			this.event_key = eventKey;
			this.rsn = rsn;
		}
	}

	public static class LocationData
	{
		public WorldCoords world;
		public LocalCoords local;

		public LocationData(WorldCoords world, LocalCoords local)
		{
			this.world = world;
			this.local = local;
		}
	}

	public static class WorldCoords
	{
		public int x;
		public int y;
		public int plane;

		public WorldCoords(int x, int y, int plane)
		{
			this.x = x;
			this.y = y;
			this.plane = plane;
		}
	}

	public static class LocalCoords
	{
		public int sceneX;
		public int sceneY;

		public LocalCoords(int sceneX, int sceneY)
		{
			this.sceneX = sceneX;
			this.sceneY = sceneY;
		}
	}

	public static class ItemData
	{
		public int slot;
		public int id;
		public int quantity;

		public ItemData(int slot, int id, int quantity)
		{
			this.slot = slot;
			this.id = id;
			this.quantity = quantity;
		}
	}

	public static class ApiResponse
	{
		public boolean success;
		public String message;

		public ApiResponse(boolean success, String message)
		{
			this.success = success;
			this.message = message;
		}

		public static ApiResponse error(String message)
		{
			return new ApiResponse(false, message);
		}
	}
}