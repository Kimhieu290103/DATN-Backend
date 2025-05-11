package dtn.ServiceScore.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

@Service
public class SupabaseService {
    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.apiKey}")
    private String apiKey;

    @Value("${supabase.bucket}")
    private String bucket;

    public String uploadFile(byte[] data, String fileName) throws IOException {
        String uploadUrl = String.format("%s/storage/v1/object/%s/%s", supabaseUrl, bucket, fileName);

        HttpURLConnection conn = (HttpURLConnection) new URL(uploadUrl).openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Authorization", "Bearer " + apiKey);
        conn.setRequestProperty("Content-Type", "application/octet-stream");

        try (OutputStream os = conn.getOutputStream()) {
            os.write(data);
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200 || responseCode == 201) {
            return supabaseUrl + "/storage/v1/object/public/" + bucket + "/" + fileName;
        } else {
            throw new RuntimeException("Upload thất bại: " + responseCode);
        }
    }
}
