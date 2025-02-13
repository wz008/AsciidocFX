package com.kodedu.service.ui.impl;

import com.kodedu.component.HtmlPane;
import com.kodedu.controller.ApplicationController;
import com.kodedu.service.ThreadService;
import com.kodedu.service.ui.IndikatorService;

import javafx.animation.Timeline;
import javafx.scene.control.ProgressBar;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by usta on 01.09.2014.
 */
@Component
public class IndikatorServiceImpl implements IndikatorService {

    private final ApplicationController applicationContoller;
    private final ThreadService threadService;
    private final HtmlPane htmlPane;

    @Autowired
    public IndikatorServiceImpl(final ApplicationController applicationContoller, final ThreadService threadService, HtmlPane htmlPane) {
        this.applicationContoller = applicationContoller;
        this.threadService = threadService;
        this.htmlPane = htmlPane;
    }

    @Override
    public void startProgressBar() {

        threadService.runActionLater(() -> {
            ProgressBar progressBar = applicationContoller.getProgressBar();
            Timeline timeline = applicationContoller.getProgressBarTimeline();
            progressBar.setVisible(true);
            progressBar.setManaged(true);
            timeline.playFromStart();
        });
    }

    @Override
    public void stopProgressBar() {
        threadService.runActionLater(() -> {
            ProgressBar progressBar = applicationContoller.getProgressBar();
            Timeline timeline = applicationContoller.getProgressBarTimeline();
            progressBar.setVisible(false);
            progressBar.setManaged(false);
            timeline.stop();
        });
    }

}
