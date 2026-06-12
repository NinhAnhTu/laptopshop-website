package com.example.laptopshop.util;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Service
public class UploadService {

    private final Cloudinary cloudinary;
    public UploadService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }
    public String handleSaveUploadFile(MultipartFile file, String targetFolder) {

        if (file.isEmpty()) {
            return "";
        }

        try {

            Map uploadResult = cloudinary.uploader().upload(
                    file.getBytes(),
                    ObjectUtils.asMap(
                            "folder", targetFolder
                    )
            );

            return uploadResult.get("secure_url").toString();

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

    }
}