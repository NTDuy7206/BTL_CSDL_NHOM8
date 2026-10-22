package com.ntd.csdl.service;

import com.ntd.csdl.exception.StorageUnavailableException;
import io.minio.BucketExistsArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.Http;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class MinioAvatarStorageService {
    private static final long MAX_AVATAR_BYTES = 5L * 1024 * 1024;
    private final MinioClient minioClient;
    private final MinioClient presignClient;

    @Value("${app.minio.bucket}")
    private String bucket;

    public MinioAvatarStorageService(
            @Qualifier("minioClient") MinioClient minioClient,
            @Qualifier("minioPresignClient") MinioClient presignClient
    ) {
        this.minioClient = minioClient;
        this.presignClient = presignClient;
    }

    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) throw new IllegalArgumentException("Vui lòng chọn ảnh đại diện.");
        if (file.getSize() > MAX_AVATAR_BYTES) throw new IllegalArgumentException("Ảnh đại diện không được lớn hơn 5 MB.");
        String contentType = file.getContentType();
        String extension = switch (contentType == null ? "" : contentType.toLowerCase()) {
            case "image/jpeg" -> ".jpg";
            case "image/png" -> ".png";
            case "image/webp" -> ".webp";
            default -> throw new IllegalArgumentException("Chỉ hỗ trợ ảnh JPG, PNG hoặc WEBP.");
        };
        String objectKey = "avatars/" + UUID.randomUUID() + extension;
        try (InputStream input = file.getInputStream()) {
            ensureBucket();
            minioClient.putObject(PutObjectArgs.builder().bucket(bucket).object(objectKey)
                    .stream(input, file.getSize(), -1L).contentType(contentType).build());
            return objectKey;
        } catch (Exception exception) {
            throw new StorageUnavailableException("Không tải được ảnh lên MinIO. Hãy kiểm tra MinIO và thông tin kết nối.", exception);
        }
    }

    public String presignedUrl(String objectKey) {
        try {
            return presignClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                    .method(Http.Method.GET).bucket(bucket).object(objectKey).expiry(1, TimeUnit.DAYS).build());
        } catch (Exception exception) {
            throw new StorageUnavailableException("Không tạo được liên kết xem ảnh từ MinIO.", exception);
        }
    }

    public void delete(String objectKey) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucket).object(objectKey).build());
        } catch (Exception ignored) {
            // Old avatars are best-effort cleanup; profile changes should still succeed.
        }
    }

    private void ensureBucket() throws Exception {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
        }
    }
}
