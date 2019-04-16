package com.cefalo.resumeparser.service;

import com.cefalo.resumeparser.core.resume.parser.ResumeParserEngine;
import com.cefalo.resumeparser.model.Resume;
import com.fasterxml.jackson.databind.JsonNode;
import io.vavr.control.Try;
import org.apache.tomcat.util.codec.binary.Base64;
import org.apache.tomcat.util.codec.binary.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ResumeParserService {

  private final ResumeParserEngine parserEngine;
  private static ExecutorService executors = Executors.newFixedThreadPool(2);

  public ResumeParserService(@Autowired final ResumeParserEngine pdfBoxResumeParserEngine) {
    this.parserEngine = pdfBoxResumeParserEngine;
  }

  public Resume parse(final byte[] fileContent) {
    Resume resume = new Resume();
    try {
      List<Callable<Boolean>> dataPopulates = createDataPopulateCallables(resume, fileContent);
      executors.invokeAll(dataPopulates);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    return resume;
  }

  private List<Callable<Boolean>> createDataPopulateCallables(final Resume resume, byte[] fileContent) {
    List<Callable<Boolean>> dataPopulates = new ArrayList<>();
    dataPopulates.add(() -> {
      populateAutomaticData(resume, fileContent);
      return true;
    });
    dataPopulates.add(() -> {
      populateProfileImage(resume, fileContent);
      return true;
    });
    return dataPopulates;
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

  private void populateProfileImage(final Resume resume, final byte[] fileContent) {
    Try<byte[]> imageTry = parserEngine.extractImage(fileContent);
    if (!imageTry.isFailure()) {
      final String imageDataUri = "data:image/jpg;base64," +
          StringUtils.newStringUtf8(Base64.encodeBase64(imageTry.get(), false));
      resume.setImage(imageDataUri);
    }
  }
}
