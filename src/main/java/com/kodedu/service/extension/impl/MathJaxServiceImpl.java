package com.kodedu.service.extension.impl;

import com.kodedu.config.ExtensionConfigBean;
import com.kodedu.controller.ApplicationController;
import com.kodedu.helper.IOHelper;
import com.kodedu.other.Current;
import com.kodedu.other.TrimWhite;
import com.kodedu.service.ThreadService;
import com.kodedu.service.cache.BinaryCacheService;
import com.kodedu.service.extension.MathJaxService;

import javafx.concurrent.Worker;
import javafx.embed.swing.SwingFXUtils;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import netscape.javascript.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Created by usta on 25.12.2014.
 */
@Component
public class MathJaxServiceImpl implements MathJaxService {

    private final Logger logger = LoggerFactory.getLogger(MathJaxService.class);

    private final ApplicationController controller;
    private final Current current;
    private final ThreadService threadService;
    private final BinaryCacheService binaryCacheService;
    private final ExtensionConfigBean extensionConfigBean;
    private WebView webView;
    private boolean initialized;

    @Value("${application.mathjax.url}")
    private String mathjaxUrl;

    @Autowired
    public MathJaxServiceImpl(final ApplicationController controller, final Current current, ThreadService threadService, BinaryCacheService binaryCacheService, ExtensionConfigBean extensionConfigBean) {
        this.controller = controller;
        this.current = current;
        this.threadService = threadService;
        this.binaryCacheService = binaryCacheService;
        this.extensionConfigBean = extensionConfigBean;
    }

    private void initialize(Runnable... runnable) {

        webEngine().getLoadWorker().stateProperty().addListener((observableValue1, state, state2) -> {
            if (state2 == Worker.State.SUCCEEDED) {
                JSObject window = getWindow();
                if (window.getMember("afx").equals("undefined"))
                    window.setMember("afx", controller);

                if (!initialized) {
                    for (Runnable run : runnable) {
                        run.run();
                    }
                }

                initialized = true;
            }
        });

        this.load();
    }

    private void load() {
        threadService.runActionLater(() -> {
            webEngine().load(String.format(mathjaxUrl, controller.getPort()));
        });
    }

    public void reload() {
        this.load();
    }

    private WebEngine webEngine() {
        return getWebView().getEngine();
    }

    public void processFormula(String formula, String imagesDir, String imageTarget) {

        threadService.runActionLater(() -> {

            if (initialized) {
                getWindow().call("processFormula", formula, imagesDir, imageTarget);
            } else {
                initialize(() -> {
                    getWindow().call("processFormula", formula, imagesDir, imageTarget);
                });
            }

        });
    }

    public JSObject getWindow() {
        return (JSObject) webEngine().executeScript("window");
    }

    public WebView getWebView() {
        if (Objects.isNull(webView)) {
            webView = new WebView();
            webView.setMaxHeight(1000);
            webView.setPrefHeight(1000);
            webView.setMaxWidth(1000);
            webView.setPrefWidth(1000);
            webView.setLayoutX(-35000);
            webView.setLayoutY(-35000);
            webView.setZoom(extensionConfigBean.getDefaultImageZoom());
            controller.getRootAnchor().getChildren().add(webView);
        }
        return webView;
    }

    public void snapshotFormula(String formula, String imagesDir, String imageTarget) {

        try {
            Objects.requireNonNull(imageTarget);

            boolean cachedResource = imageTarget.contains("/afx/cache");

            if (!imageTarget.endsWith(".png") && !cachedResource)
                return;

            Integer cacheHit = current.getCache().get(imageTarget);

            int hashCode = (imageTarget + imagesDir + formula).hashCode();

            if (Objects.isNull(cacheHit) || hashCode != cacheHit) {

                WritableImage writableImage = getWebView().snapshot(new SnapshotParameters(), null);
                BufferedImage bufferedImage = SwingFXUtils.fromFXImage(writableImage, null);

                Path path = current.currentTab().getParentOrWorkdir();

                threadService.runTaskLater(() -> {
                    try {
                        TrimWhite trimWhite = new TrimWhite();
                        BufferedImage trimmed = trimWhite.trim(bufferedImage);
                        if (!cachedResource) {
                            Path imagePath = path.resolve(imageTarget);
                            IOHelper.createDirectories(imagePath.getParent());
                            IOHelper.imageWrite(trimmed, "png", imagePath.toFile());
                            threadService.runActionLater(() -> {
                                controller.clearImageCache(imagePath);
                            });
                        } else {
                            binaryCacheService.putBinary(imageTarget, trimmed);
                            threadService.runActionLater(() -> {
                                controller.clearImageCache(imageTarget);
                            });
                        }

                        current.getCache().put(imageTarget, hashCode);
                        logger.debug("MathJax extension is ended for {}", imageTarget);

                    } catch (Exception e) {
                        logger.error("Problem occured while generating MathJax png", e);
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (Exception e) {
            logger.error("Problem occured while generating MathJax png", e);
            throw e;
        }


    }
}
