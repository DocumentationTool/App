package com.wonkglorg.doc.api.controller;

import com.wonkglorg.doc.core.exception.client.ClientException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.file.Path;

import static com.wonkglorg.doc.api.controller.Constants.ControllerPaths.CONVERT;

@RestController
@RequestMapping(CONVERT)
public class ConvertController {
    private static final Logger log = LoggerFactory.getLogger(ConvertController.class);
    private final FileService fileService;

    public ConvertController(@Lazy FileService fileService) {
        this.fileService = fileService;
    }


    /**
     * Converts a word document to markdown
     *
     * @return The markdown representation of the word document
     */
    public ResponseEntity<RestResponse<String>> convertWordToMarkdown() {
        try {
            return RestResponse.success(fileService.convertWordToMarkdown(Path.of("")).get()).toResponse();
        } catch (ClientException e) {
            return RestResponse.<String>error(e.getMessage()).toResponse();
        } catch (Exception e) {
            log.error("Error while checking edited state ", e);
            return RestResponse.<String>error(e.getMessage()).toResponse();
        }
    }


}
