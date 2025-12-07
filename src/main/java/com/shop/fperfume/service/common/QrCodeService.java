package com.shop.fperfume.service.common;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

@Service
public class QrCodeService {

    public String generatePaymentQrBase64(String maDon, BigDecimal soTien) {
        // ✅ format số tiền (nên để đơn vị VNĐ, không dấu chấm để chắc ăn)
        String amountStr = soTien != null ? soTien.toPlainString() : "0";

        // TODO: thay bằng thông tin tài khoản thật của bạn
        String bankName = "MBBank";
        String accountNumber = "0397682148";
        String accountName = "NGUYEN THE MANH";

        // Nội dung QR – bạn có thể chỉnh format tuỳ thích
        String content = "Thanh toan don: " + maDon + "\n"
                + "Ngan hang: " + bankName + "\n"
                + "STK: " + accountNumber + "\n"
                + "Chu tai khoan: " + accountName + "\n"
                + "So tien: " + amountStr + " VND\n"
                + "Noi dung: " + maDon;

        return generateBase64(content, 220, 220);
    }

    public String generateBase64(String text, int width, int height) {
        try {
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name());
            hints.put(EncodeHintType.MARGIN, 1);

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

            BufferedImage bufferedImage = MatrixToImageWriter.toBufferedImage(bitMatrix);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos);
            byte[] bytes = baos.toByteArray();

            return Base64.getEncoder().encodeToString(bytes);
        } catch (WriterException | java.io.IOException e) {
            throw new RuntimeException("Lỗi tạo QR code", e);
        }
    }
}
