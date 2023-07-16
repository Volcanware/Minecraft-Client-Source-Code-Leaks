package me.aidanmees.trivia.client.events;

public abstract class Event {

	private boolean cancelled;

	public boolean isCancelled() {
		return cancelled;
	}

	public void cancel() {
		this.cancelled = true;
	}

}
