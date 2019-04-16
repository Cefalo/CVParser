package com.cefalo.resumeparser.service;

import com.cefalo.resumeparser.core.resume.parser.ResumeParserEngine;
import com.cefalo.resumeparser.model.Resume;
import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.control.Try;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResumeParserService {

  private final ResumeParserEngine parserEngine;

  public ResumeParserService(@Autowired final ResumeParserEngine pdfBoxResumeParserEngine) {
    this.parserEngine = pdfBoxResumeParserEngine;
  }

  public Resume parse(final byte[] fileContent) {
    Resume resume = new Resume();
    populateAutomaticData(resume, fileContent);
    return resume;
  }

  private void populateAutomaticData(final Resume resume, byte[] fileContent) {
    Try<JsonNode> jsonNodes = parserEngine.parseFile(fileContent);
    if (jsonNodes.isSuccess()) {
      JsonNode resultNode = jsonNodes.get();
      if (resultNode.has("title")) {
        JsonNode rootTitleNode = resultNode.get("title");
        if (rootTitleNode.has("probableName")) {
          String probableName = rootTitleNode.get("probableName").textValue();
          resume.setName(probableName);
        }
      }

      if (resultNode.has("email")) {
        JsonNode rootEmailNode = resultNode.get("email");
        if (rootEmailNode.has("email")) {
          String email = rootEmailNode.get("email").textValue();
          resume.setEmail(email);
        }
      }

      if (resultNode.has("phone_number")) {
        JsonNode rootPhoneNode = resultNode.get("phone_number");
        if (rootPhoneNode.has("phone")) {
          String email = rootPhoneNode.get("phone").textValue();
          resume.setPhone(email);
        }
      }
    }
  }
}
