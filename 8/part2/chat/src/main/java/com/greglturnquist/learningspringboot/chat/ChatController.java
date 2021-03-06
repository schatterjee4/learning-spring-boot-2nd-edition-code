/*
 * Copyright 2016 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.greglturnquist.learningspringboot.chat;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * @author Greg Turnquist
 */
// tag::code[]
@Controller
public class ChatController {

	private final SimpMessagingTemplate template;

	public ChatController(SimpMessagingTemplate template) {
		this.template = template;
	}

	@MessageMapping("/chatMessage.new")
	@SendTo("/topic/chatMessage.new")
	public String newChatMessage(String newChatMessage,
								 SimpMessageHeaderAccessor
									 headerAccessor) {
		return headerAccessor.getSessionId()+ ": " + newChatMessage;
	}

}
// end::code[]
