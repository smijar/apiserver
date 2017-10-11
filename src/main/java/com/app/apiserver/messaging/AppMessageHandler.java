package com.app.apiserver.messaging;

import com.app.apiserver.core.AppMessage;

public interface AppMessageHandler {
	 public void onMessage(AppMessage message);
}
