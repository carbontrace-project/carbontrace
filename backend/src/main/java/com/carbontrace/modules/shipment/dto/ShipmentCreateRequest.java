package com.carbontrace.modules.shipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body of {@code POST /api/shipments} (COMMANDO.md Section 8.4) — the
 * four fields of that section's example, in its order.
 *
 * <p>Called AFTER the browser's presigned PUT has succeeded. The bytes are
 * already in S3 by this point; this call only registers what landed there, which
 * is why it carries a key rather than a file.
 *
 * <p>{@code contentType} is deliberately absent. Section 9 allows only
 * {@code application/pdf}, and STEP A016 already signed the upload with that
 * content type — S3 rejected anything else at PUT time. Letting the client
 * re-declare it here would create a second, unverifiable source of truth, so the
 * service writes the constant instead.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentCreateRequest {

    @NotNull(message = "Vendor id is required")
    private Long vendorId;

    /**
     * The key returned by {@code POST /api/shipments/upload-url}.
     *
     * <p>{@code @Size(max = 300)} matches the Section 7 column so an over-long
     * key is a 400 rather than a database error. Uniqueness is enforced by the
     * UNIQUE constraint on {@code shipment_documents.s3_key}, which surfaces as
     * 409 through the Section 23 mapping — a duplicate means the same object is
     * being registered twice.
     */
    @NotBlank(message = "S3 key is required")
    @Size(max = 300, message = "S3 key must not exceed 300 characters")
    private String s3Key;

    @NotBlank(message = "File name is required")
    @Size(max = 200, message = "File name must not exceed 200 characters")
    private String fileName;

    /**
     * Nullable in Section 7, so optional here. It is the browser's report of
     * what it uploaded — informational, never trusted; the object in S3 is the
     * only authority on its own size.
     */
    @Positive(message = "File size must be greater than zero")
    private Long fileSizeBytes;
}
