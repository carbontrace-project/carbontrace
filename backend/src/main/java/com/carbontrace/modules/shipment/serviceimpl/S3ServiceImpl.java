package com.carbontrace.modules.shipment.serviceimpl;

import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.carbontrace.common.AppConstants;
import com.carbontrace.exception.BadRequestException;
import com.carbontrace.modules.shipment.dto.UploadUrlResponse;
import com.carbontrace.modules.shipment.service.S3Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

/**
 * Presigned URL generation against the private bucket (COMMANDO.md Sections 9
 * and 17).
 *
 * <p>Logging rule for this whole class: a presigned URL is a bearer credential —
 * anyone holding it can write or read the object until it expires. Section 21
 * forbids logging one in full, so every log line here goes through
 * {@link #withoutSignature(String)}, and the key is logged instead of the URL
 * wherever that is enough.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private static final String PDF_CONTENT_TYPE = "application/pdf";
    private static final String INVALID_CONTENT_TYPE = "Only application/pdf files are accepted";
    private static final String FILE_TOO_LARGE = "File exceeds the maximum upload size of 10 MB";
    private static final String BUCKET_NOT_CONFIGURED =
            "S3 bucket is not configured — set the S3_BUCKET environment variable (MANUAL SETUP C)";

    /** Section 17 key convention: {@code invoices/{yyyy}/{MM}/{uuid}-{sanitizedFileName}}. */
    private static final String KEY_PREFIX = "invoices";
    private static final DateTimeFormatter KEY_YEAR = DateTimeFormatter.ofPattern("yyyy");
    private static final DateTimeFormatter KEY_MONTH = DateTimeFormatter.ofPattern("MM");

    /** Section 17: sanitise the file name to this character set. Everything else becomes '_'. */
    private static final String ILLEGAL_KEY_CHARS = "[^a-zA-Z0-9._-]";
    private static final String REDACTED_QUERY = "?<signature-redacted>";

    /**
     * Longest sanitised file name allowed into a key. The Section 7 column is
     * {@code VARCHAR(300)} and the fixed part of a key —
     * {@code invoices/yyyy/MM/} plus a 36-character UUID and a hyphen — is 56
     * characters, so this leaves the key comfortably inside the column even
     * before the DTO's own 200-character cap applies.
     */
    private static final int MAX_KEY_FILENAME_LENGTH = 200;

    private final S3Presigner s3Presigner;

    /**
     * Field-injected for the same reason as {@code AuthServiceImpl}: Lombok does
     * not copy {@code @Value} onto {@code @RequiredArgsConstructor} parameters.
     *
     * <p>Defaults to empty in {@code application.yml}, so an unconfigured
     * environment is caught here with a message naming the fix rather than
     * failing inside the AWS SDK.
     */
    @Value("${app.aws.s3-bucket}")
    private String bucket;

    @Override
    public UploadUrlResponse generatePresignedPut(String fileName, String contentType, Long declaredSize) {
        requireBucket();
        validateContentType(contentType);
        validateSize(declaredSize);

        String key = buildKey(fileName);

        // contentType is part of the signature: S3 rejects the PUT unless the
        // browser sends exactly this header back (Section 12). That is what stops
        // a signed URL for a PDF being reused to upload something else.
        PutObjectRequest objectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(AppConstants.PRESIGNED_PUT_EXPIRY_MINUTES))
                .putObjectRequest(objectRequest)
                .build();

        String url = s3Presigner.presignPutObject(presignRequest).url().toString();
        log.info("Presigned PUT issued for key={} (expires in {} min)", key, AppConstants.PRESIGNED_PUT_EXPIRY_MINUTES);
        log.debug("Presigned PUT target: {}", withoutSignature(url));

        return UploadUrlResponse.builder()
                .uploadUrl(url)
                .s3Key(key)
                .expiresInSeconds(AppConstants.PRESIGNED_PUT_EXPIRY_MINUTES * 60)
                .build();
    }

    @Override
    public String generatePresignedGet(String s3Key) {
        requireBucket();

        GetObjectRequest objectRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(AppConstants.PRESIGNED_GET_EXPIRY_MINUTES))
                .getObjectRequest(objectRequest)
                .build();

        String url = s3Presigner.presignGetObject(presignRequest).url().toString();
        log.info("Presigned GET issued for key={} (expires in {} min)", s3Key, AppConstants.PRESIGNED_GET_EXPIRY_MINUTES);
        log.debug("Presigned GET target: {}", withoutSignature(url));

        return url;
    }

    // ----------------------------------------------------------------- helpers

    /** Section 9: "Only PDF files ({@code application/pdf})". */
    private void validateContentType(String contentType) {
        if (!PDF_CONTENT_TYPE.equalsIgnoreCase(contentType)) {
            log.warn("Upload rejected — content type was {}", contentType);
            throw new BadRequestException(INVALID_CONTENT_TYPE);
        }
    }

    /**
     * Section 9: "maximum 10 MB (validated at upload-url time via declared
     * size)". A null size means the caller declared none, which the Section 8.4
     * body permits — there is then nothing to check here.
     */
    private void validateSize(Long declaredSize) {
        if (declaredSize != null && declaredSize > AppConstants.MAX_UPLOAD_BYTES) {
            log.warn("Upload rejected — declared size {} bytes exceeds the {} byte limit",
                    declaredSize, AppConstants.MAX_UPLOAD_BYTES);
            throw new BadRequestException(FILE_TOO_LARGE);
        }
    }

    private void requireBucket() {
        if (bucket == null || bucket.isBlank()) {
            log.error("app.aws.s3-bucket is not set — presigning cannot proceed");
            throw new BadRequestException(BUCKET_NOT_CONFIGURED);
        }
    }

    /**
     * Builds {@code invoices/{yyyy}/{MM}/{uuid}-{sanitizedFileName}} (Section 17).
     *
     * <p>The UUID is what makes the key unique, so two uploads of the same file
     * name never collide and the UNIQUE constraint on {@code s3_key} only ever
     * fires on a genuine double-registration.
     *
     * <p>Sanitisation is a whitelist, not a blacklist: every character outside
     * {@code [a-zA-Z0-9._-]} becomes an underscore. Slashes therefore cannot
     * survive, so a file named {@code ../../etc/passwd} cannot climb out of the
     * {@code invoices/} prefix — it becomes {@code .._.._etc_passwd}.
     */
    private String buildKey(String fileName) {
        LocalDate today = LocalDate.now();
        String sanitized = fileName.replaceAll(ILLEGAL_KEY_CHARS, "_");
        if (sanitized.length() > MAX_KEY_FILENAME_LENGTH) {
            sanitized = sanitized.substring(0, MAX_KEY_FILENAME_LENGTH);
        }
        return "%s/%s/%s/%s-%s".formatted(
                KEY_PREFIX,
                today.format(KEY_YEAR),
                today.format(KEY_MONTH),
                UUID.randomUUID(),
                sanitized);
    }

    /**
     * Strips the query string, which carries {@code X-Amz-Signature} and
     * {@code X-Amz-Credential} — the parts that make the URL usable
     * (COMMANDO.md Sections 13 and 21: never log a presigned URL in full).
     */
    private String withoutSignature(String url) {
        int queryStart = url.indexOf('?');
        return queryStart < 0 ? url : url.substring(0, queryStart) + REDACTED_QUERY;
    }
}
