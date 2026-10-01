package com.bancoias.transfers.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.bancoias.transfers.domain.policy.DailyLimitPolicy;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;

class DailyLimitPolicyTest {

	private static final BigDecimal DAILY_LIMIT = BigDecimal.valueOf(5_000_000);

	@Test
	void doesNotExceedWhenConsumedPlusAmountIsBelowLimit() {
		boolean exceeds = DailyLimitPolicy.exceedsDailyLimit(
				BigDecimal.valueOf(1_000_000), BigDecimal.valueOf(2_000_000), DAILY_LIMIT);

		assertThat(exceeds).isFalse();
	}

	@Test
	void doesNotExceedWhenConsumedPlusAmountEqualsLimitExactly() {
		boolean exceeds = DailyLimitPolicy.exceedsDailyLimit(
				BigDecimal.valueOf(4_000_000), BigDecimal.valueOf(1_000_000), DAILY_LIMIT);

		assertThat(exceeds).isFalse();
	}

	@Test
	void exceedsWhenConsumedPlusAmountGoesOverLimitByOne() {
		boolean exceeds = DailyLimitPolicy.exceedsDailyLimit(
				BigDecimal.valueOf(4_500_000), BigDecimal.valueOf(500_001), DAILY_LIMIT);

		assertThat(exceeds).isTrue();
	}
}
