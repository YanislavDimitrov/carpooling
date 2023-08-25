package com.example.carpooling.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:application.properties")
public class CloudinaryConfig {
    @Bean
    public Cloudinary getCloudinary() {

        return new Cloudinary(ObjectUtils.asMap(
                "cloud_name", "dqq0hqmgs",
                "api_key", "577692632984128",
                "api_secret", "CUQb--nD_qazzWkFDumu1s9GbJ8"));
    }
}
