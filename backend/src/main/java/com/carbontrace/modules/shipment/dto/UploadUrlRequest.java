package com.carbontrace.modules.shipment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body of {@code POST /api/shipments/upload-url} (COMMANDO.md
 * Section 8.4).
 *
 * <p>The Section 8.4 example carries exactly {@code fileName} and
 * {@code contentType}, and a request with only those two fields is valid here —
 * the frozen contract of Section 25.2 is preserved verbatim.
 *
 * <p>{@code fileSizeBytes} is an ADDITIVE, OPTIONAL third field. Section 9
 * requires the 10 MB ceiling to be "validated at upload-url time via declared
 * size", which is impossible if the request never declares one; the browser
 * knows the size before it uploads, so declaring it lets the server refuse an
 * oversized file before any bytes cross the network. Being optional, it cannot
 * break a client written against the Section 8.4 example, and Track B's mock
 * stays correct.
 *
 * <p>The two Section 9 content rules — PDF only, 10 MB maximum — are enforced in
 * {@code S3ServiceImpl} rather than by annotations here, so there is a single
 * enforcement point that also covers the STEP A017 shipment-creation path.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UploadUrlRequest {

    /**
     * The auditor's original file name. Sanitised to {@code [a-zA-Z0-9._-]}
     * before it becomes part of the S3 key (Section 17), so nothing here can
     * shape the key beyond that character set.
     */
    @NotBlank(message = "File name is required")
    @Size(max = 200, message = "File name must not exceed 200 characters")
    private String fileName;

    @NotBlank(message = "Content type is required")
    @Size(max = 100, message = "Content type must not exceed 100 characters")
    private String contentType;

    /**
     * Declared size in bytes. Optional; when present it must be a positive
     * number and is checked against {@code AppConstants.MAX_UPLOAD_BYTES}.
     *
     * <p>Declared, not verified — it is a browser-supplied hint. Section 9 says
     * the limit is also "enforced in the presigned policy where practical",
     * which is the guarantee that survives a lying client.
     */
    @Positive(message = "File size must be greater than zero")
    private Long fileSizeBytes;
}
