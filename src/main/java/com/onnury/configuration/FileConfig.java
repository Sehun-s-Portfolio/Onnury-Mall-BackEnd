package com.onnury.configuration;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class FileConfig implements WebMvcConfigurer {

    // http://116.125.141.139:8080/music/dfbauhrlbvald.png
    // 리소스 접근 시 addResourceHandler를 통해 요청 경로 값을 설정하고
    // add ResourceLocations를 통해 접근할 파일 이들어가져있는 실질적인 경로를 입력받아
    // 요청 url + addResourceHandlers에서 설정한 경로 값을 요청하면 locations 에 설정한 파일들에 실질적으로 접근할 수 있또록 하는 Config 함수
    // 예) http://116.125.141.139:8080/music/dfbauhrlbvald.png 을 요청하면 locations에 설정한 접근 경로 중 하나에 일치한 파일에 접근한다.
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler( "/image/**")
                .addResourceLocations("file:///dntnas/image/", "file:///dntnas/image/banner/",
                        "file:///dntnas/image/label/", "file:///dntnas/image/product/",
                        "file:///dntnas/image/product/detail/", "file:///dntnas/image/category/",
                        "file:///datanas/image/", "file:///datanas/image/banner/",
                        "file:///datanas/image/label/", "file:///datanas/image/product/",
                        "file:///datanas/image/product/detail/", "file:///datanas/image/category/",
                        "file:///10.28.102.9:/dntnas/image/", "file:///10.28.102.9:/dntnas/image/banner/",
                        "file:///10.28.102.9:/dntnas/image/label/", "file:///10.28.102.9:/dntnas/image/product/",
                        "file:///10.28.102.9:/dntnas/image/product/detail/", "file:///10.28.102.9:/dntnas/image/category/",
                        "file:///home/onnury/web/image/", "file:///home/onnury/web/image/banner/",
                        "file:///home/onnury/web/image/label/", "file:///home/onnury/web/image/product/",
                        "file:///home/onnury/web/image/product/detail/", "file:///home/onnury/web/image/category/",
                        "file:///datanas/", "file:///10.28.102.9:/dntnas/",
                        "file:///home/onnury/web/image/", "file:///home/onnury/web/image/banner/",
                        "file:///home/onnury/web/image/label/", "file:///home/onnury/web/image/product/",
                        "file:///home/onnury/web/image/product/detail/", "file:///home/onnury/web/image/category/",
                        "file:///home/onnury/web/image/brand/", "file:///home/onnury/web/image/faq/",
                        "file:///home/onnury/web/image/notices/", "file:///home/onnury/web/image/inquiry/");

        // 이후 배포 서버를 변경하게 될 때 그에 맞춰서 실제 이미지가 업로드될 경로를 지정하는 addResourceLocations 를 다시 지정해주어야 하고,
        // 마찬가지로 실제 배포 서버에서도 업로드 경로에 맞게끔 폴더 경로들을 생성해주어야 한다.
    }

    /**
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/image/**")
                .addResourceLocations("file:///home/onnury/web/image/", "file:///home/onnury/web/image/banner/",
                        "file:///home/onnury/web/image/label/", "file:///home/onnury/web/image/product/",
                        "file:///home/onnury/web/image/product/detail/", "file:///home/onnury/web/image/category/");

        // 이후 배포 서버를 변경하게 될 때 그에 맞춰서 실제 이미지가 업로드될 경로를 지정하는 addResourceLocations 를 다시 지정해주어야 하고,
        // 마찬가지로 실제 배포 서버에서도 업로드 경로에 맞게끔 폴더 경로들을 생성해주어야 한다.
    }
    **/
}
