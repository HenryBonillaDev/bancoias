package com.bancoias.transfers.api.dto;

import com.bancoias.transfers.domain.model.AccountDailyUsage;

public final class AccountDtoMapper {

	private AccountDtoMapper() {
	}

	public static AccountDailyUsageResponseDto toResponse(AccountDailyUsage usage) {
		return new AccountDailyUsageResponseDto(
				usage.accountId(), usage.dailyLimit(), usage.consumedToday(), usage.remaining());
	}
}
