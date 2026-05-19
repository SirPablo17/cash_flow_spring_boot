package pablo.nasc.cash_flow_spring_boot.controllers;

import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springdoc.webmvc.api.MultipleOpenApiWebMvcResource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class ApiDocsVersionController {

    private final MultipleOpenApiWebMvcResource multipleOpenApiResource;

    @Value("${springdoc.api-docs.path:/internal/api-docs}")
    private String apiDocsPath;

    @GetMapping(value = "/v1/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> v1Docs(HttpServletRequest request, Locale locale)
            throws JsonProcessingException {
        return docs(request, "v1", locale);
    }

    @GetMapping(value = "/v2/api-docs", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<byte[]> v2Docs(HttpServletRequest request, Locale locale)
            throws JsonProcessingException {
        return docs(request, "v2", locale);
    }

    private ResponseEntity<byte[]> docs(HttpServletRequest request,
                                        String group,
                                        Locale locale) throws JsonProcessingException {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(multipleOpenApiResource.openapiJson(request, apiDocsPath, group, locale));
    }
}
