package com.anonLove.dto.response.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateChatRoomResponse {
    @JsonProperty("room_id")
    private Long roomId;
}
