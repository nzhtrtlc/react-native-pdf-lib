
package com.hopding.pdflib;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.FileProvider;
import android.util.Log;

import com.facebook.react.bridge.NoSuchKeyException;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import com.facebook.react.bridge.ReadableMap;
import com.hopding.pdflib.factories.PDDocumentFactory;
import com.tom_roush.pdfbox.pdmodel.PDDocument;
import com.tom_roush.pdfbox.pdmodel.PDPage;
import com.tom_roush.pdfbox.pdmodel.PDPageContentStream;
import com.tom_roush.pdfbox.pdmodel.font.PDFont;
import com.tom_roush.pdfbox.pdmodel.font.PDType1Font;
import com.tom_roush.pdfbox.util.PDFBoxResourceLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class PDFLibModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    public PDFLibModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;

        PDFBoxResourceLoader.init(reactContext);
    }

    @Override
    public String getName() {
        return "PDFLib";
    }

    @ReactMethod
    public void createPDF(ReadableMap documentActions, Promise promise) {
        try {
            PDDocument document = PDDocumentFactory.create(documentActions);
            promise.resolve(PDDocumentFactory.write(document, documentActions.getString("path")));
        } catch (NoSuchKeyException e) {
            e.printStackTrace();
            promise.reject(e);
        } catch (IOException e) {
            e.printStackTrace();
            promise.reject(e);
        }
    }

    @ReactMethod
    public void modifyPDF(ReadableMap documentActions, Promise promise) {
        try {
            PDDocument document = PDDocumentFactory.modify(documentActions);
            promise.resolve(PDDocumentFactory.write(document, documentActions.getString("path")));
        } catch (NoSuchKeyException e) {
            e.printStackTrace();
            promise.reject(e);
        } catch (IOException e) {
            e.printStackTrace();
            promise.reject(e);
        }
    }

    @ReactMethod
    public void test(String text, Promise promise) {
        File dir = new File(reactContext.getFilesDir().getPath() + "/pdfs");
        dir.mkdirs();

        String pdfPath = dir + "/test.pdf";

        PDDocument document = new PDDocument();
        PDPage page1 = new PDPage();
        PDPage page2 = new PDPage();
        document.addPage(page2);
        document.addPage(page1);

        PDPageContentStream contentStream1;
        PDPageContentStream contentStream2;
        try {
            contentStream1 = new PDPageContentStream(document, page1);
            contentStream1.addRect(5, 500, 100, 100);
            contentStream1.setNonStrokingColor(0, 255, 125);
            contentStream1.fill();
            contentStream1.close();

            PDFont font = PDType1Font.HELVETICA;
            contentStream2 = new PDPageContentStream(document, page2);
            contentStream2.beginText();
            contentStream2.setNonStrokingColor(15, 38, 192);
            contentStream2.setFont(font, 12);
            contentStream2.newLineAtOffset(100, 700);
            contentStream2.showText(text);
            contentStream2.endText();
            contentStream2.close();

            document.save(pdfPath);
            document.close();

            promise.resolve(pdfPath);
        } catch (IOException e) {
            e.printStackTrace();
            promise.reject(e);
        }
    }

    @ReactMethod
    public void getDocumentsDirectory(Promise promise) {
        promise.resolve(reactContext.getFilesDir().getPath());
    }

    @ReactMethod
    public void unloadAsset(String assetName, String destPath, Promise promise) {
        try {
            InputStream is = reactContext.getAssets().open(assetName);
            byte[] buffer = new byte[is.available()];
            is.read(buffer);
            is.close();

            File destFile = new File(destPath);
            File dirFile = new File(destFile.getParent());
            dirFile.mkdirs();

            FileOutputStream fos = new FileOutputStream(destFile);
            fos.write(buffer);
            fos.close();
            promise.resolve(destPath);
        } catch (IOException e) {
            promise.reject(e);
        }
    }

    @ReactMethod
    public void stripText(ReadableMap documentActions, Promise promise) {
        String path = documentActions.getString("path");
        String parsedText = null;
        PDDocument document = null;
        try {
            document = PDDocument.load(path);
        } catch (IOException e) {
            promise.reject(e);
        }
        try {
            PDFTextStripper pdfStripper = new PDFTextStripper();
            pdfStripper.setStartPage(0);
            pdfStripper.setEndPage(1);
            parsedText = pdfStripper.getText(document);
        } catch (Exception e) {
            promise.reject(e);
        } finally {
            try {
                if (document != null) document.close();
            } catch (Exception e) {
                promise.reject(e);
            }
        }
        promise.resolve(parsedText);
    }

    @ReactMethod
    public void getAssetPath(String assetName, Promise promise) {
        promise.reject(new Exception(
                "PDFLib.getAssetPath() is only available on iOS. Try PDFLib.unloadAsset()"
        ));
    }
}
