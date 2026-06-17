package live.nxasenpai.NxaSenpai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

/**
 * Serves uploaded photos from the local uploads/photos/ directory
 * via the /photos/** URL path.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = Paths.get("uploads/photos")
                .toAbsolutePath().toUri().toString();

        registry.addResourceHandler("/photos/**")
                .addResourceLocations(uploadPath);
    }
}
