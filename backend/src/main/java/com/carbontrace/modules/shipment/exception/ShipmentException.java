package com.carbontrace.modules.shipment.exception;

import com.carbontrace.exception.GlobalExceptionHandler;

/**
 * Thrown when a shipment-specific rule is violated (COMMANDO.md Section 9,
 * "Upload / Shipment Rules"). Mapped to HTTP 400 by
 * {@link GlobalExceptionHandler} (COMMANDO.md Section 23).
 *
 * <p>Section 6 gives the shipment module its own exception, separate from the
 * shared {@code BadRequestException}, because the rules it carries are specific
 * to this module's state machine and upload constraints — an illegal status
 * transition, a non-PDF content type, an over-size file, a second document on a
 * shipment, or a calculation attempted without the fields Section 9 requires.
 *
 * <p>Both map to 400, so the distinction is not about the status code: it is
 * about being able to catch, log and evolve shipment failures without touching
 * every other module's error handling. Compare {@code PurchaseException}, which
 * Section 23 maps to 409 instead.
 *
 * <p>The rules themselves are enforced from STEP A017 onward; this class only
 * gives them a type.
 */
public class ShipmentException extends RuntimeException {

    public ShipmentException(String message) {
        super(message);
    }
}
