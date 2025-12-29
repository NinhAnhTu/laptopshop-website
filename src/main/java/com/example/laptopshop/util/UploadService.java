//package com.example.laptopshop.util;
//
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.BufferedOutputStream;
//import java.io.File;
//import java.io.FileOutputStream;
//import java.nio.file.Paths;
//
//@Service
//public class UploadService {
//
//    public String handleSaveUploadFile(MultipartFile file, String targetFolder) {
//        // Tên file rỗng -> return null
//        if (file.isEmpty()) return null;
//
//        try {
//            // 1. Xác định đường dẫn lưu file (Vào thư mục target/classes/static/uploads để hiện ngay)
//            // Lưu ý: Đường dẫn này trỏ vào thư mục resources gốc của dự án
//            String rootPath = System.getProperty("user.dir") + File.separator + "uploads" + File.separator + targetFolder;
//
//            File dir = new File(rootPath);
//            if (!dir.exists()) {
//                dir.mkdirs();
//            }
//
//            String finalName = System.currentTimeMillis() + "-" + file.getOriginalFilename();
//
//            File serverFile = new File(dir.getAbsolutePath() + File.separator + finalName);
//            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
//            stream.write(file.getBytes());
//            stream.close();
//
//            return "/uploads/" + targetFolder + "/" + finalName;
//
//        } catch (Exception e) {
//            e.printStackTrace();
//            return null;
//
//        }
//    }
//}

package com.example.laptopshop.util;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Service
public class UploadService {

    public String handleSaveUploadFile(MultipartFile file, String targetFolder) {
        // 1. Kiểm tra file rỗng
        if (file.isEmpty()) return "";

        try {
            String rootPath = System.getProperty("user.dir") + File.separator + "uploads";

            File dir = new File(rootPath + File.separator + targetFolder);

            if (!dir.exists()) {
                dir.mkdirs();
            }

            String finalName = System.currentTimeMillis() + "-" + file.getOriginalFilename();

            File serverFile = new File(dir.getAbsolutePath() + File.separator + finalName);

            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(serverFile));
            stream.write(file.getBytes());
            stream.close();

            return "/uploads/" + targetFolder + "/" + finalName;

        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }
    }
}