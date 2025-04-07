package com.fix.game_service.domain;

import lombok.Getter;

@Getter
public enum Team {

	KIA("KIA 타이거즈"),
	SAMSUNG("삼성 라이온즈"),
	LG("LG 트윈스"),
	BEARS("두산 베어스"),
	KT("KT 위즈"),
	SSG("SSG 렌더스");

	private final String teamName;

	Team(String teamName) {
		this.teamName = teamName;
	}

}