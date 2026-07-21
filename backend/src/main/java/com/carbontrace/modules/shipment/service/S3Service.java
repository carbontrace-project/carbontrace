package com.carbontrace.modules.shipment.service;

import com.carbontrace.modules.shipment.dto.UploadUrlResponse;

/**
 * Presigned-URL generation for the private document bucket (COMMANDO.md
 * Sections 8.4, 9 and 17).
 *
 * <p>This service never touches file bytes. The browser uploads straight to S3
 * and FastAPI downloads straight from it; Spring Boot only ever hands out
 * short-lived signed URLs (Section 18, Flows 3 and 4). That is what keeps a
 * 10 MB PDF off the application's heap and out of its request path.
 */
public interface S3Service {

    /**
     * Signs a {@code PUT} for a new object under the Section 17 key convention.
     *
     * @param fileName     the auditor's original file name; sanitised before use
     * @param contentType  must be {@code application/pdf} (Section 9)
     * @param declaredSize declared size in bytes, or null if the caller did not
     *                     declare one; when present it must not exceed
     *                     {@code AppConstants.MAX_UPLOAD_BYTES}
     * @return the signed URL, the generated key, and the expiry in seconds
     * @throws com.carbontrace.exception.BadRequestException if the content type is
     *         not a PDF or the declared size exceeds the limit
     */
    UploadUrlResponse generatePresignedPut(String fileName, String contentType, Long declaredSize);

    /**
     * Signs a {@code GET} for an existing object so the frontend can display or
     * download the original PDF (Section 8.4).
     *
     * <p>Generated fresh on every call rather than stored: a stored URL would
     * either be long-lived — defeating the point of a private bucket — or
     * already expired by the time anyone clicked it.
     *
     * @param s3Key key of an object already registered in {@code shipment_documents}
     * @return the signed URL, valid for {@code AppConstants.PRESIGNED_GET_EXPIRY_MINUTES}
     */
    String generatePresignedGet(String s3Key);
}
