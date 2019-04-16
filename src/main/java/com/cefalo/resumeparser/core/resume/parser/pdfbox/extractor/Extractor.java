package com.cefalo.resumeparser.core.resume.parser.pdfbox.extractor;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.pdfbox.pdmodel.PDDocument;

import java.util.Optional;

public interface Extractor {
  Optional<JsonNode> extract(PDDocument document);

  String getKeyName();
}
