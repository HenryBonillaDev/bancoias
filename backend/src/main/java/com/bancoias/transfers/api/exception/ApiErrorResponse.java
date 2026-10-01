package com.bancoias.transfers.api.exception;

import java.util.List;

public record ApiErrorResponse(String message, List<String> details) {
}
