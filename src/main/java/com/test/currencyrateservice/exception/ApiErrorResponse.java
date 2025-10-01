package com.test.currencyrateservice.exception;

import java.time.OffsetDateTime;

public record ApiErrorResponse(OffsetDateTime timestamp, int status, String error, String path) {}
