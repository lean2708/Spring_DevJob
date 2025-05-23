package spring_devjob.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import spring_devjob.constants.FileType;
import spring_devjob.entity.FileEntity;
import spring_devjob.exception.FileException;
import spring_devjob.repository.FileRepository;
import spring_devjob.service.FileService;

import java.io.IOException;
import java.util.*;


@Slf4j(topic = "FILE-SERVICE")
@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService {

    private final Cloudinary cloudinary;
    private final FileRepository fileRepository;

    private static final List<String> IMAGE_TYPES = Arrays.asList("image/jpeg", "image/png", "image/gif", "image/webp");
    private static final List<String> VIDEO_TYPES = Arrays.asList("video/mp4", "video/avi", "video/mov", "video/mkv");
    private static final List<String> CV_TYPES = Arrays.asList("application/pdf", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "text/plain");

    @Value("${cloud.folder-image}")
    private String folderImage;

    @Value("${cloud.max-size-image}")
    private String maxSizeImage;

    @Value("${cloud.folder-video}")
    private String folderVideo;

    @Value("${cloud.max-size-video}")
    private String maxSizeVideo;

    @Value("${cloud.folder-cv}")
    private String folderCV;

    @Value("${cloud.max-size-cv}")
    private String maxSizeCV;


    private long parseSize(String size) {
        size = size.toUpperCase();
        return Long.parseLong(size.replace("MB", "").trim()) * 1024 * 1024;
    }

    @Override
    public FileEntity uploadFile(MultipartFile file, FileType type) throws IOException, FileException {
        if (file == null || file.isEmpty()) {
            throw new FileException("File trống. Không thể lưu trữ file");
        }
        String folder = determineUploadFolder(file, type);

        Map<String, Object> options = ObjectUtils.asMap("folder", folder);

        if (type == FileType.VIDEO) {
            options.put("resource_type", "video");
        } else if (type == FileType.CV) {
            options.put("resource_type", "raw");
        }

        Map uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

        FileEntity fileEntity = FileEntity.builder()
                .id(uploadResult.get("public_id").toString())
                .fileName(file.getOriginalFilename())
                .type(type.name())
                .url(uploadResult.get("url").toString())
                .build();

        return fileRepository.save(fileEntity);
    }

    private String determineUploadFolder(MultipartFile file, FileType type) throws FileException {
        switch (type){
            case IMAGE -> {
                validateFile(file, IMAGE_TYPES, maxSizeImage, "Ảnh");
                return folderImage;
            }
            case VIDEO -> {
                validateFile(file, VIDEO_TYPES, maxSizeVideo, "Video");
                return folderVideo;
            }
            case CV -> {
                validateFile(file, CV_TYPES, maxSizeCV, "CV");
                return folderCV;
            }
            default -> throw new FileException("Loại file không hỗ trợ");
        }
    }

    private void validateFile(MultipartFile file, List<String> validTypes, String maxSize, String fileType) throws FileException {
        if (!validTypes.contains(file.getContentType())) {
            throw new FileException("File " + file.getOriginalFilename() + " không hợp lệ. Định dạng file không được hỗ trợ.");
        }
        if (file.getSize() > parseSize(maxSize)) {
            throw new FileException(fileType + " quá lớn! Chỉ được tối đa " + maxSize + ".");
        }
    }

    @Override
    public List<FileEntity> getAllFiles() {
        return fileRepository.findAll();
    }

    @Override
    public Boolean deleteFile(String publicId) throws Exception {
        FileEntity fileEntity = fileRepository.findById(publicId)
                .orElseThrow(()-> new FileException("File không tồn tại trong hệ thống"));

        String resourceType = switch (FileType.valueOf(fileEntity.getType())) {
            case IMAGE -> "image";
            case VIDEO -> "video";
            case CV -> "raw";
        };

        Map<String, Object> options = ObjectUtils.asMap("resource_type", resourceType);
        cloudinary.uploader().destroy(publicId, options);

        fileRepository.delete(fileEntity);
        return true;
    }

}

