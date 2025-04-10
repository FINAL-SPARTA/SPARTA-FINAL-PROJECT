package com.fix.game_service.domain.model;

public enum GameStatus {

	PLAY("진행"),
	CANCEL("취소");

	private final String name;

	GameStatus(String name) {
		this.name = name;
	}

}

