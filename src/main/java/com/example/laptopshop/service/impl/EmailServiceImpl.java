package com.example.laptopshop.service.impl;

import com.example.laptopshop.entity.ChatMessage;
import com.example.laptopshop.entity.Order;
import com.example.laptopshop.entity.User;
import com.example.laptopshop.entity.Warranty;
import com.example.laptopshop.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.email.from}")
    private String fromEmail;

    @Value("${app.email.admin-email}")
    private String adminEmail;

    @Override
    @Async
    public void sendOrderStatusEmail(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED, StandardCharsets.UTF_8.name());

            Context context = new Context();
            context.setVariable("order", order);
            String html = templateEngine.process("mail/order_status", context);

            helper.setTo(order.getUser().getEmail());
            helper.setFrom(fromEmail);
            helper.setSubject("Cập nhật trạng thái đơn hàng #" + order.getOrderId());
            helper.setText(html, true);

            mailSender.send(message);
            System.out.println("Đã gửi mail cập nhật trạng thái cho đơn: " + order.getOrderId());

        } catch (MessagingException e) {
            e.printStackTrace();
            System.out.println("Lỗi gửi mail: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendResetPasswordEmail(String to, String token) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String resetLink = "http://localhost:8080/reset-password?token=" + token;

            String htmlContent = "<h3>Yêu cầu đặt lại mật khẩu</h3>"
                    + "<p>Ai đó đã yêu cầu đặt lại mật khẩu cho tài khoản liên kết với email này.</p>"
                    + "<p>Vui lòng nhấn vào link bên dưới để đặt mật khẩu mới (Link hết hạn sau 15 phút):</p>"
                    + "<a href=\"" + resetLink + "\">Đặt lại mật khẩu ngay</a>"
                    + "<p>Nếu bạn không yêu cầu, vui lòng bỏ qua email này.</p>";

            helper.setTo(to);
            helper.setFrom(fromEmail);
            helper.setSubject("Yêu cầu đặt lại mật khẩu - Laptop Shop");
            helper.setText(htmlContent, true);

            mailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    @Async
    public void sendNewPasswordEmail(String toEmail, String newPassword) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Thông báo thay đổi mật khẩu - Laptop Shop");

            String content = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd;'>"
                    + "<h2 style='color: #d9534f;'>Cập nhật thông tin tài khoản</h2>"
                    + "<p>Xin chào,</p>"
                    + "<p>Quản trị viên vừa thực hiện thay đổi mật khẩu cho tài khoản của bạn.</p>"
                    + "<p>Mật khẩu mới của bạn là: <b style='font-size: 18px; color: #0275d8;'>" + newPassword + "</b></p>"
                    + "<p>Vui lòng đăng nhập và đổi lại mật khẩu nếu cần thiết.</p>"
                    + "<br>"
                    + "<p>Trân trọng,<br>Đội ngũ Laptop Shop</p>"
                    + "</div>";

            helper.setText(content, true);

            mailSender.send(message);

            System.out.println("Đã gửi mail mật khẩu mới cho: " + toEmail);

        } catch (MessagingException e) {
            System.out.println("Lỗi gửi mail: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendWarrantyExpirationEmail(Warranty warranty) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("customerName", warranty.getUser().getFullname());
            context.setVariable("productName", warranty.getProduct().getProductName());
            context.setVariable("warrantyCode", warranty.getWarrantyCode());
            String expDate = warranty.getExpirationDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            context.setVariable("expirationDate", expDate);

            String htmlContent = templateEngine.process("mail/warranty_expiration", context);

            helper.setTo(warranty.getUser().getEmail());
            helper.setSubject("⚠️ [LaptopShop] Thông báo sắp hết hạn bảo hành - " + warranty.getProduct().getProductName());
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Đã gửi mail nhắc bảo hành cho: " + warranty.getUser().getEmail());

        } catch (MessagingException e) {
            System.out.println("Lỗi gửi mail bảo hành: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendNewMessageNotification(ChatMessage message) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            String senderName = message.getSender().getFullname();
            String content = message.getContent();

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 15px; border: 1px solid #ccc;'>"
                    + "<h3 style='color: #007bff;'>💬 Tin nhắn hỗ trợ mới</h3>"
                    + "<p>Khách hàng <b>" + senderName + "</b> vừa gửi một tin nhắn:</p>"
                    + "<blockquote style='background: #f9f9f9; padding: 10px; border-left: 5px solid #007bff;'>"
                    + content
                    + "</blockquote>"
                    + "<p>Vui lòng truy cập trang quản trị để phản hồi.</p>"
                    + "</div>";

            helper.setFrom(fromEmail);
            helper.setTo(adminEmail);
            helper.setSubject("🔔 [Hỗ trợ] Tin nhắn mới từ " + senderName);
            helper.setText(htmlContent, true);

            mailSender.send(mimeMessage);
            System.out.println("Đã gửi thông báo tin nhắn mới tới Admin: " + adminEmail);

        } catch (MessagingException e) {
            System.out.println("Lỗi gửi mail thông báo chat: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendOrderCancellationNotification(Order order) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String customerName = order.getUser().getFullname();
            String orderId = String.valueOf(order.getOrderId());

            String totalAmount = "0";
            if (order.getFinalAmount() != null) {
                totalAmount = java.text.NumberFormat.getIntegerInstance(java.util.Locale.GERMANY).format(order.getFinalAmount());
            }

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #ddd; max-width: 600px; margin: 0 auto;'>"
                    + "<h2 style='color: #d9534f; text-align: center;'>⚠️ THÔNG BÁO HỦY ĐƠN HÀNG</h2>"
                    + "<p>Xin chào Admin,</p>"
                    + "<p>Khách hàng <b>" + customerName + "</b> vừa thực hiện hủy đơn hàng <b>#" + orderId + "</b>.</p>"
                    + "<div style='background-color: #f9f9f9; padding: 15px; border-radius: 5px; margin: 15px 0;'>"
                    + "<ul style='list-style-type: none; padding: 0;'>"
                    + "<li style='margin-bottom: 10px;'>📦 <b>Mã đơn hàng:</b> #" + orderId + "</li>"
                    + "<li style='margin-bottom: 10px;'>👤 <b>Khách hàng:</b> " + customerName + "</li>"
                    + "<li style='margin-bottom: 10px;'>💰 <b>Tổng hoàn tiền (dự kiến):</b> <span style='color: #d9534f; font-weight: bold;'>" + totalAmount + " đ</span></li>"
                    + "<li style='margin-bottom: 10px;'>📅 <b>Ngày đặt:</b> " + order.getCreatedAt() + "</li>"
                    + "</ul>"
                    + "</div>"
                    + "<p>Hệ thống đã tự động hoàn lại số lượng tồn kho (Restock) cho các sản phẩm trong đơn hàng này.</p>"
                    + "<div style='text-align: center; margin-top: 25px;'>"
                    + "<a href='http://localhost:8080/admin/orders' style='background-color: #007bff; color: white; padding: 12px 25px; text-decoration: none; border-radius: 5px; font-weight: bold;'>Xem Chi Tiết Đơn Hàng</a>"
                    + "</div>"
                    + "</div>";

            helper.setFrom(fromEmail);
            helper.setTo(adminEmail);
            helper.setSubject("⚠️ [Hủy Đơn] Khách hàng " + customerName + " đã hủy đơn hàng #" + orderId);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Đã gửi thông báo hủy đơn #" + orderId + " tới Admin: " + adminEmail);

        } catch (MessagingException e) {
            System.out.println("Lỗi gửi mail hủy đơn: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendVoucherGiftNotification(User user, BigDecimal totalSpent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(user.getEmail());
            helper.setSubject("🎉 CHÚC MỪNG! BẠN NHẬN ĐƯỢC VOUCHER TỪ CHAOSCODERS");

            DecimalFormat formatter = new DecimalFormat("#,###");
            String formattedTotal = formatter.format(totalSpent);

            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; border: 1px solid #eee; border-radius: 10px;'>"
                    + "<h2 style='color: #f37021;'>Cảm ơn bạn đã đồng hành cùng ChaosCoders!</h2>"
                    + "<p>Xin chào <b>" + user.getFullname() + "</b>,</p>"
                    + "<p>Đơn hàng vừa rồi của bạn đã được thanh toán thành công.</p>"
                    + "<p>Tổng tích lũy mua sắm của bạn hiện tại là: <b style='color: #0f2027; font-size: 18px;'>" + formattedTotal + " đ</b></p>"
                    + "<div style='background-color: #e3f2fd; padding: 15px; border-radius: 5px; margin: 20px 0;'>"
                    + "<h3 style='margin-top: 0; color: #0277bd;'>🎁 QUÀ TẶNG DÀNH RIÊNG CHO BẠN</h3>"
                    + "<p>Dựa trên hạng thành viên tích lũy, chúng tôi đã mở khóa thêm các <b>Mã giảm giá đặc biệt</b> cho đơn hàng tiếp theo của bạn.</p>"
                    + "<p>👉 Hãy truy cập trang <b>Thanh toán</b> và mở <b>Kho Voucher</b> để kiểm tra và sử dụng ngay nhé!</p>"
                    + "</div>"
                    + "<p>Trân trọng,<br/>Đội ngũ ChaosCoders</p>"
                    + "</div>";

            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Đã gửi mail tặng voucher cho: " + user.getEmail());

        } catch (Exception e) {
            System.out.println("Lỗi gửi mail voucher: " + e.getMessage());
        }
    }
    @Override
    @Async
    public void sendReviewReplyEmail(com.example.laptopshop.entity.Review review) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("review", review);

            String htmlContent = templateEngine.process("mail/email_review_reply", context);

            helper.setFrom(fromEmail);
            helper.setTo(review.getUser().getEmail());
            helper.setSubject("💬 Shop đã trả lời đánh giá của bạn - ChaosCoders");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Đã gửi mail trả lời review cho: " + review.getUser().getEmail());

        } catch (MessagingException e) {
            System.out.println("Lỗi gửi mail review reply: " + e.getMessage());
        }
    }

    @Override
    @Async
    public void sendReviewRemovedEmail(com.example.laptopshop.entity.Review review, String reason) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            Context context = new Context();
            context.setVariable("review", review);
            context.setVariable("reason", reason);

            String htmlContent = templateEngine.process("mail/email_review_removed", context);

            helper.setFrom(fromEmail);
            helper.setTo(review.getUser().getEmail());
            helper.setSubject("⚠️ Thông báo gỡ bỏ đánh giá - ChaosCoders");
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Đã gửi mail gỡ review cho: " + review.getUser().getEmail());

        } catch (MessagingException e) {
            System.out.println("Lỗi gửi mail review removed: " + e.getMessage());
        }
    }
    @Override
    @Async
    public void sendAdminReviewAlert(com.example.laptopshop.entity.Review review) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            String subject = "⚠️ [ALERT] Đánh giá thấp - " + review.getProduct().getProductName();

            String htmlContent = "<div style='font-family: Arial; border: 2px solid #dc3545; padding: 20px; border-radius: 10px;'>"
                    + "<h2 style='color: #dc3545; margin-top: 0;'>⚠️ CẢNH BÁO ĐÁNH GIÁ THẤP</h2>"
                    + "<p>Hệ thống vừa nhận được một đánh giá tiêu cực (dưới 5 sao). AI đã <b>TỪ CHỐI</b> trả lời.</p>"
                    + "<hr style='border: 0; border-top: 1px solid #eee;'/>"
                    + "<p>👤 <b>Khách hàng:</b> " + review.getUser().getFullname() + "</p>"
                    + "<p>📦 <b>Sản phẩm:</b> " + review.getProduct().getProductName() + "</p>"
                    + "<p>⭐ <b>Số sao:</b> <span style='color: orange; font-size: 18px; font-weight: bold;'>" + review.getRating() + "/5</span></p>"
                    + "<div style='background-color: #f8d7da; padding: 15px; border-radius: 5px; color: #721c24; margin: 10px 0;'>"
                    + "<i>\"" + review.getComment() + "\"</i>"
                    + "</div>"
                    + "<br/>"
                    + "<a href='http://localhost:8080/admin/reviews' style='background-color: #dc3545; color: white; padding: 12px 20px; text-decoration: none; border-radius: 5px; font-weight: bold;'>TRUY CẬP ADMIN ĐỂ XỬ LÝ</a>"
                    + "</div>";

            helper.setTo(adminEmail);
            helper.setFrom(fromEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            mailSender.send(message);
            System.out.println("Đã gửi cảnh báo review thấp tới Admin: " + adminEmail);

        } catch (MessagingException e) {
            System.out.println("Lỗi gửi mail cảnh báo: " + e.getMessage());
        }
    }
}