package com.carbontrace.modules.shipment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response body of {@code POST /api/shipments/upload-url} (COMMANDO.md
 * Section 8.4) — the three fields of that section's example, in its order.
 *
 * <p>{@code uploadUrl} is a signed, short-lived credential in URL form. The
 * browser PUTs the file bytes straight to it with NO {@code Authorization}
 * header — the signature is already in the query string (Section 12). It is
 * returned to the caller and never logged in full (Section 21).
 *
 * <p>{@code s3Key} is the caller's handle for the object afterwards: it is what
 * {@code POST /api/shipments} sends back in STEP A017 to register the upload.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadUrlResponse {

    private String uploadUrl;
    private String s3Key;
    private int expiresInSeconds;
}
