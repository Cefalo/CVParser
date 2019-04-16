package com.cefalo.resumeparser.controller;

import com.cefalo.resumeparser.model.Resume;
import com.cefalo.resumeparser.service.ResumeParserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(value = "/parse")
public class ResumeParserController {

  private final ResumeParserService resumeParserService;

  public ResumeParserController(@Autowired final ResumeParserService resumeParserService) {
    this.resumeParserService = resumeParserService;
  }

  @PostMapping(value = "/resume", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
  public ResponseEntity<Resume> parse(@RequestParam("file") MultipartFile pdf) throws IOException {
    if (StringUtils.equalsIgnoreCase(pdf.getContentType(), MediaType.APPLICATION_PDF_VALUE)) {
      return ResponseEntity.ok(resumeParserService.parse(pdf.getBytes()));
    }
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .build();
  }
}
