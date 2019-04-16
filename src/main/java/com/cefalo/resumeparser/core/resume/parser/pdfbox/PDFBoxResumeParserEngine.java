package com.cefalo.resumeparser.core.resume.parser.pdfbox;

import com.cefalo.resumeparser.core.resume.parser.ResumeParserEngine;
import com.cefalo.resumeparser.core.resume.parser.pdfbox.extractor.EmailExtractor;
import com.cefalo.resumeparser.core.resume.parser.pdfbox.extractor.Extractor;
import com.cefalo.resumeparser.core.resume.parser.pdfbox.extractor.PhoneNumberExtractor;
import com.cefalo.resumeparser.core.resume.parser.pdfbox.extractor.TitleExtractor;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.vavr.control.Try;
import org.apache.pdfbox.cos.COSName;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDResources;
import org.apache.pdfbox.pdmodel.graphics.PDXObject;
import org.apache.pdfbox.pdmodel.graphics.form.PDFormXObject;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * CV Parser Engine implementation based on PDFBox library
 */
@Service("pdfBoxResumeParserEngine")
public class PDFBoxResumeParserEngine implements ResumeParserEngine {

  private static final ObjectMapper objectMapper = new ObjectMapper();

  private List<Extractor> extractors = new ArrayList<>();

  public PDFBoxResumeParserEngine(
      @Autowired final TitleExtractor titleExtractor,
      @Autowired final EmailExtractor emailExtractor,
      @Autowired final PhoneNumberExtractor phoneNumberExtractor
  ) {
    extractors.addAll(
        Arrays.asList(
            titleExtractor,
            emailExtractor,
            phoneNumberExtractor
        )
    );
  }

  @Override
  public Try<JsonNode> parseFile(byte[] fileContent) {
    try {
      ObjectNode resultNode = objectMapper.createObjectNode();

      PDDocument pdfDocument = PDDocument.load(fileContent);
      for (Extractor extractor : extractors) {
        Optional<JsonNode> resultOption = extractor.extract(pdfDocument);
        resultOption.ifPresent(childNode -> {
          resultNode.set(extractor.getKeyName(), childNode);
        });
      }

      return Try.success(resultNode);
    } catch (IOException e) {
      e.printStackTrace();
      return Try.failure(e);
    }
  }

  public Try<byte[]> extractImage(byte[] fileContent) {
    try {
      PDDocument pdDocument = PDDocument.load(fileContent);
      List<RenderedImage> imageList = getImagesFromPDF(pdDocument);

      if (imageList.size() <= 0) {
        return Try.failure(new RuntimeException("No embedded image found in the document"));
      }

      RenderedImage firstImage = null;
      for (RenderedImage renderedImage : imageList) {
        if (renderedImage.getHeight() >= 128) {
          firstImage = renderedImage;
          break;
        }
      }

      if (firstImage == null) {
        return Try.failure(new RuntimeException("No embedded image with height greater than 128px is found"));
      }

      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
      ImageIO.write(firstImage, "jpg", outputStream);

      byte[] imageBytes = outputStream.toByteArray();

      if (imageBytes.length == 0) {
        return Try.failure(new RuntimeException("Image file extraction problem, zero byte image"));
      }

      return Try.success(imageBytes);
    } catch (IOException e) {
      return Try.failure(e);
    }
  }

  private List<RenderedImage> getImagesFromPDF(PDDocument document) throws IOException {
    List<RenderedImage> images = new ArrayList<>();
    for (PDPage page : document.getPages()) {
      images.addAll(getImagesFromResources(page.getResources()));
    }

    return images;
  }

  private List<RenderedImage> getImagesFromResources(PDResources resources) throws IOException {
    List<RenderedImage> images = new ArrayList<>();

    for (COSName xObjectName : resources.getXObjectNames()) {
      PDXObject xObject = resources.getXObject(xObjectName);

      if (xObject instanceof PDFormXObject) {
        images.addAll(getImagesFromResources(((PDFormXObject) xObject).getResources()));
      } else if (xObject instanceof PDImageXObject) {
        images.add(((PDImageXObject) xObject).getImage());
      }
    }

    return images;
  }
}
