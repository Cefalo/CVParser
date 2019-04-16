package com.cefalo.resumeparser.core.resume.parser.pdfbox.extractor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * There can be multiple email addresses. candidates, references etc.
 * Make sure to take the first one, which is ideally the email address of the candidate.
 * And only from the first page.
 */
@Component
public class EmailExtractor implements Extractor {
  private static final Pattern VALID_EMAIL_ADDRESS_REGEX =
      Pattern.compile("[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}", Pattern.CASE_INSENSITIVE);

  @Override
  public Optional<JsonNode> extract(PDDocument document) {
    try {
      ObjectMapper objectMapper = new ObjectMapper();
      ObjectNode result = objectMapper.createObjectNode();

      PDFTextStripper textStripper = new PDFTextStripper();
      textStripper.setEndPage(1);
      String firstPageText = textStripper.getText(document);
      Optional<String> emailOpt = extractEmailFromText(firstPageText);
      if (emailOpt.isPresent()) {
        result.put("email", emailOpt.get());
        return Optional.of(result);
      } else {
        return Optional.empty();
      }
    } catch (IOException e) {
      e.printStackTrace();
      return Optional.empty();
    }
  }


  private static Optional<String> findFirstEmail(String content) {
    Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(content);

    if (matcher.find()) {
      String firstEmail = matcher.group();
      return Optional.of(firstEmail);
    } else {
      System.out.println("No matching email found with regex.");
      return Optional.empty();
    }
  }

  private Optional<String> extractEmailFromText(String rawText) {
    return findFirstEmail(rawText);
  }

  @Override
  public String getKeyName() {
    return "email";
  }
}
